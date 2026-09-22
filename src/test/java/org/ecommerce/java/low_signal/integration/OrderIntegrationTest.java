package org.ecommerce.java.low_signal.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.ecommerce.java.low_signal.dto.CreateOrderRequest;
import org.ecommerce.java.low_signal.dto.OrderDTO;
import org.ecommerce.java.low_signal.model.*;
import org.ecommerce.java.low_signal.repository.*;
import org.ecommerce.java.low_signal.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Product product;
    private ProductVariant variant1;
    private ProductVariant variant2;
    private UserAddress userAddress;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("test@email.com");
        user.setPassword("Password1!");
        user.setPhone("1234567890");
        user = userRepository.save(user);

        jdbcTemplate.update(
                "INSERT INTO collections (collection_status, created_at, description, featured, hero_image, name, season, slug) "
                        + "VALUES ('AVAILABLE', CURRENT_TIMESTAMP, 'Test collection', false, 'https://example.com/img.jpg', 'Test Collection', 'SS25', 'test-collection')");

        Integer collectionId = jdbcTemplate.queryForObject(
                "SELECT id FROM collections WHERE slug = 'test-collection'", Integer.class);
        Collection collection = entityManager.find(Collection.class, collectionId);

        product = new Product();
        product.setName("TEST HOODIE");
        product.setSlug("test-hoodie");
        product.setTag("TEST");
        product.setDescription("Test product for integration tests");
        product.setPrice(new BigDecimal("148.00"));
        product.setComparePrice(new BigDecimal("192.00"));
        product.setFeatured(false);
        product.setCollection(collection);
        product = productRepository.save(product);

        variant1 = new ProductVariant();
        variant1.setSize("M");
        variant1.setStockQuantity(10);
        variant1.setSku("TEST-HOODIE-M");
        variant1.setProduct(product);
        variant1 = productVariantRepository.save(variant1);

        variant2 = new ProductVariant();
        variant2.setSize("L");
        variant2.setStockQuantity(5);
        variant2.setSku("TEST-HOODIE-L");
        variant2.setProduct(product);
        variant2 = productVariantRepository.save(variant2);

        Address address = new Address();
        address.setFullName("Test User");
        address.setStreet("Via Test 1");
        address.setCity("Milano");
        address.setPostalCode("20100");
        address.setProvince("Milano");
        address.setCountry("IT");
        address = addressRepository.save(address);

        userAddress = new UserAddress();
        userAddress.setFullName("Test User");
        userAddress.setStreet("Via Test 1");
        userAddress.setCity("Milano");
        userAddress.setPostalCode("20100");
        userAddress.setProvince("Milano");
        userAddress.setCountry("IT");
        userAddress.setIsDefault(true);
        userAddress.setUser(user);
        userAddress = userAddressRepository.save(userAddress);
    }

    private CreateOrderRequest buildRequest(Integer productId, Integer variantId, Integer quantity) {
        CreateOrderRequest request = new CreateOrderRequest();
        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setProductVariantId(variantId);
        item.setQuantity(quantity);
        request.setItems(List.of(item));
        request.setShippingAddressId(userAddress.getId());
        request.setBillingAddressId(userAddress.getId());
        return request;
    }

    @Test
    void createOrder_stockSufficiente_StockRidottoNelDatabase() {
        CreateOrderRequest request = buildRequest(product.getId(), variant1.getId(), 3);

        OrderDTO result = orderService.createOrder(user.getId(), request);

        assertNotNull(result);
        assertEquals("PENDING", result.getPaymentStatus());

        ProductVariant updated = productVariantRepository.findById(variant1.getId()).orElseThrow();
        assertEquals(7, updated.getStockQuantity());
    }

    @Test
    void createOrder_stockInsufficiente_NonModificaStock() {
        CreateOrderRequest request = buildRequest(product.getId(), variant1.getId(), 15);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(user.getId(), request));

        assertTrue(ex.getMessage().contains("Insufficient stock"));

        ProductVariant updated = productVariantRepository.findById(variant1.getId()).orElseThrow();
        assertEquals(10, updated.getStockQuantity());
    }

    @Test
    void createOrder_multipliItem_StockRidottoPerOgniVariante() {
        CreateOrderRequest.OrderItemRequest item1 = new CreateOrderRequest.OrderItemRequest();
        item1.setProductId(product.getId());
        item1.setProductVariantId(variant1.getId());
        item1.setQuantity(3);

        CreateOrderRequest.OrderItemRequest item2 = new CreateOrderRequest.OrderItemRequest();
        item2.setProductId(product.getId());
        item2.setProductVariantId(variant2.getId());
        item2.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item1, item2));
        request.setShippingAddressId(userAddress.getId());
        request.setBillingAddressId(userAddress.getId());

        OrderDTO result = orderService.createOrder(user.getId(), request);

        assertNotNull(result);
        assertEquals(2, result.getOrderItems().size());

        ProductVariant updated1 = productVariantRepository.findById(variant1.getId()).orElseThrow();
        ProductVariant updated2 = productVariantRepository.findById(variant2.getId()).orElseThrow();
        assertEquals(7, updated1.getStockQuantity());
        assertEquals(3, updated2.getStockQuantity());
    }
}
