package org.ecommerce.java.low_signal.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.ecommerce.java.low_signal.exception.PaymentException;
import org.ecommerce.java.low_signal.model.Address;
import org.ecommerce.java.low_signal.model.Collection;
import org.ecommerce.java.low_signal.model.Order;
import org.ecommerce.java.low_signal.model.OrderItem;
import org.ecommerce.java.low_signal.model.PaymentStatus;
import org.ecommerce.java.low_signal.model.Product;
import org.ecommerce.java.low_signal.model.ProductVariant;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.AddressRepository;
import org.ecommerce.java.low_signal.repository.OrderRepository;
import org.ecommerce.java.low_signal.repository.ProductRepository;
import org.ecommerce.java.low_signal.repository.ProductVariantRepository;
import org.ecommerce.java.low_signal.repository.StripeWebhookEventRepository;
import org.ecommerce.java.low_signal.repository.UserRepository;
import org.ecommerce.java.low_signal.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;

/**
 * Fine del flusso di pagamento: dal corpo firmato di Stripe fino allo stato
 * dell'ordine e al magazzino. Nessuna chiamata di rete: la firma viene
 * costruita a mano esattamente come fa Stripe.
 */
@SpringBootTest
@ActiveProfiles("test")
class WebhookIntegrationTest {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private StripeWebhookEventRepository eventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private User user;
    private Address address;
    private Product product;
    private ProductVariant variant;

    private int sequence;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        user = new User();
        user.setName("Webhook");
        user.setSurname("Tester");
        user.setEmail("webhook-" + unique + "@email.com");
        user.setPassword("Password1!");
        user.setPhone("1234567890");
        user = userRepository.save(user);

        jdbcTemplate.update(
                "INSERT INTO collections (collection_status, created_at, description, featured, hero_image, name, season, slug) "
                        + "VALUES ('AVAILABLE', CURRENT_TIMESTAMP, 'Test', false, 'https://example.com/i.jpg', 'Test "
                        + unique + "', 'SS25', 'test-" + unique + "')");
        Integer collectionId = jdbcTemplate.queryForObject(
                "SELECT id FROM collections WHERE slug = 'test-" + unique + "'", Integer.class);
        Collection collection = entityManager.find(Collection.class, collectionId);

        product = new Product();
        product.setName("TEST HOODIE");
        product.setSlug("test-hoodie-" + unique);
        product.setTag("TEST");
        product.setDescription("Webhook integration test");
        product.setPrice(new BigDecimal("148.00"));
        product.setComparePrice(new BigDecimal("192.00"));
        product.setFeatured(false);
        product.setCollection(collection);
        product = productRepository.save(product);

        variant = new ProductVariant();
        variant.setSize("M");
        variant.setStockQuantity(7);
        variant.setSku("TEST-M-" + unique);
        variant.setProduct(product);
        variant = productVariantRepository.save(variant);

        address = new Address();
        address.setFullName("Webhook Tester");
        address.setStreet("Via Test 1");
        address.setCity("Milano");
        address.setPostalCode("20100");
        address.setProvince("MI");
        address.setCountry("IT");
        address = addressRepository.save(address);
    }

    private Order createPendingOrder(String paymentIntentId, int quantity) {
        Order order = new Order();
        order.setOrderNumber("LS-TEST-" + (++sequence) + "-" + System.nanoTime());
        order.setSubtotal(new BigDecimal("148.00").multiply(BigDecimal.valueOf(quantity)));
        order.setShippingPrice(BigDecimal.ZERO);
        order.setTotalPrice(new BigDecimal("148.00").multiply(BigDecimal.valueOf(quantity)));
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStripePaymentIntent(paymentIntentId);
        order.setUser(user);
        order.setShippingAddress(address);
        order.setBillingAddress(address);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductVariant(variant);
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setSize("M");
        item.setQuantity(quantity);
        item.setOrder(order);
        order.setOrderItems(List.of(item));

        return orderRepository.save(order);
    }

    private String payload(String eventId, String type, String paymentIntentId) {
        return "{\"id\":\"" + eventId + "\",\"type\":\"" + type + "\",\"data\":{\"object\":"
                + "{\"id\":\"" + paymentIntentId + "\",\"object\":\"payment_intent\"}}}";
    }

    private void deliver(String body, String signature) {
        webhookService.handleEvent(body.getBytes(StandardCharsets.UTF_8), signature);
    }

    private PaymentException deliverExpectingFailure(String body, String signature) {
        return assertThrows(PaymentException.class, () -> deliver(body, signature));
    }

    @Test
    void eventoValido_OrdineDiventaPaid() {
        Order order = createPendingOrder("pi_int_ok", 2);
        String body = payload("evt_int_ok", "payment_intent.succeeded", "pi_int_ok");

        deliver(body, sign(body, webhookSecret, Instant.now().getEpochSecond()));

        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(PaymentStatus.PAID, reloaded.getPaymentStatus());
    }

    @Test
    void firmaErrata_VieneRifiutataEOrdineInvariato() {
        Order order = createPendingOrder("pi_int_bad", 2);
        String body = payload("evt_int_bad", "payment_intent.succeeded", "pi_int_bad");

        PaymentException ex = deliverExpectingFailure(body, sign(body, "whsec_sbagliato",
                Instant.now().getEpochSecond()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(PaymentStatus.PENDING, reloaded.getPaymentStatus());
    }

    @Test
    void pagamentoFallito_OrdineFallitoEStockRilasciato() {
        int stockBefore = variant.getStockQuantity();
        Order order = createPendingOrder("pi_int_failed", 3);
        String body = payload("evt_int_failed", "payment_intent.payment_failed", "pi_int_failed");

        deliver(body, sign(body, webhookSecret, Instant.now().getEpochSecond()));

        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        ProductVariant variantReloaded = productVariantRepository.findById(variant.getId()).orElseThrow();

        assertEquals(PaymentStatus.FAILED, reloaded.getPaymentStatus());
        assertEquals(stockBefore + 3, variantReloaded.getStockQuantity());
    }

    @Test
    void eventoRicevutoDueVolte_StockRilasciatoUnaSolaVolta() {
        int stockBefore = variant.getStockQuantity();
        String body = payload("evt_int_dup", "payment_intent.payment_failed", "pi_int_dup");
        Order order = createPendingOrder("pi_int_dup", 3);
        String signature = sign(body, webhookSecret, Instant.now().getEpochSecond());

        deliver(body, signature);
        deliver(body, signature);

        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        ProductVariant variantReloaded = productVariantRepository.findById(variant.getId()).orElseThrow();

        assertEquals(PaymentStatus.FAILED, reloaded.getPaymentStatus());
        assertEquals(stockBefore + 3, variantReloaded.getStockQuantity());
    }

    @Test
    void eventoSconosciuto_VieneRegistratoSenzaEffettiSugliOrdini() {
        String body = payload("evt_int_unk", "payment_intent.succeeded", "pi_int_non_esiste");

        deliver(body, sign(body, webhookSecret, Instant.now().getEpochSecond()));

        // L'evento viene registrato per audit, ma nessun ordine viene toccato
        assertTrue(eventRepository.existsById("evt_int_unk"));
    }

    /** header Stripe-Signature: t=<timestamp>,v1=hmac_sha256(secret, "<t>.<payload>") */
    private static String sign(String payload, String secret, long timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
            return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile calcolare la firma di test", e);
        }
    }
}
