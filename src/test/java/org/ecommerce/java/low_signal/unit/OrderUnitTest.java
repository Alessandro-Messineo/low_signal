package org.ecommerce.java.low_signal.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.ecommerce.java.low_signal.dto.CreateOrderRequest;
import org.ecommerce.java.low_signal.dto.OrderDTO;
import org.ecommerce.java.low_signal.model.Address;
import org.ecommerce.java.low_signal.model.Product;
import org.ecommerce.java.low_signal.model.ProductVariant;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.model.UserAddress;
import org.ecommerce.java.low_signal.repository.AddressRepository;
import org.ecommerce.java.low_signal.repository.OrderRepository;
import org.ecommerce.java.low_signal.repository.ProductRepository;
import org.ecommerce.java.low_signal.repository.ProductVariantRepository;
import org.ecommerce.java.low_signal.repository.UserAddressRepository;
import org.ecommerce.java.low_signal.repository.UserRepository;
import org.ecommerce.java.low_signal.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private ProductVariant variant;
    private Address address;
    private UserAddress userAddress;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Mario");
        user.setSurname("Rossi");
        user.setEmail("mario@email.com");

        product = new Product();
        product.setId(1);
        product.setName("WRAITH HOODIE");
        product.setPrice(new BigDecimal("148.00"));

        variant = new ProductVariant();
        variant.setId(1);
        variant.setSize("M");
        variant.setStockQuantity(10);
        variant.setProduct(product);
        variant.setSku("WRAITH-HOODIE-M");

        address = new Address();
        address.setId(1);
        address.setFullName("Mario Rossi");
        address.setStreet("Via Roma 10");
        address.setCity("Milano");
        address.setPostalCode("20100");
        address.setProvince("Milano");
        address.setCountry("IT");

        userAddress = new UserAddress();
        userAddress.setId(1);
        userAddress.setFullName("Mario Rossi");
        userAddress.setStreet("Via Roma 10");
        userAddress.setCity("Milano");
        userAddress.setPostalCode("20100");
        userAddress.setProvince("Milano");
        userAddress.setCountry("IT");
        userAddress.setUser(user);
    }

    private CreateOrderRequest buildRequest(Integer productId, Integer variantId, Integer quantity) {
        CreateOrderRequest request = new CreateOrderRequest();
        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setProductVariantId(variantId);
        item.setQuantity(quantity);
        request.setItems(List.of(item));
        request.setShippingAddressId(1);
        request.setBillingAddressId(1);
        return request;
    }

    @Test
    void createOrder_stockSufficiente_RiduceStock() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(userAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = buildRequest(1, 1, 3);
        OrderDTO result = orderService.createOrder(1, request);

        assertNotNull(result);
        assertEquals(7, variant.getStockQuantity());
        verify(productVariantRepository, times(1)).save(variant);
    }

    @Test
    void createOrder_stockInsufficiente_LanciaEccezione() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(userAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));

        CreateOrderRequest request = buildRequest(1, 1, 15);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1, request));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        assertEquals(10, variant.getStockQuantity());
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    void createOrder_stockEsatto_Azzerstock() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(userAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = buildRequest(1, 1, 10);
        OrderDTO result = orderService.createOrder(1, request);

        assertNotNull(result);
        assertEquals(0, variant.getStockQuantity());
    }

    @Test
    void createOrder_multipliItem_RiduceStockOgniVariante() {
        ProductVariant variant2 = new ProductVariant();
        variant2.setId(2);
        variant2.setSize("L");
        variant2.setStockQuantity(10);
        variant2.setProduct(product);
        variant2.setSku("WRAITH-HOODIE-L");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(userAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(productVariantRepository.findById(2)).thenReturn(Optional.of(variant2));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest.OrderItemRequest item1 = new CreateOrderRequest.OrderItemRequest();
        item1.setProductId(1);
        item1.setProductVariantId(1);
        item1.setQuantity(3);

        CreateOrderRequest.OrderItemRequest item2 = new CreateOrderRequest.OrderItemRequest();
        item2.setProductId(1);
        item2.setProductVariantId(2);
        item2.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(Arrays.asList(item1, item2));
        request.setShippingAddressId(1);
        request.setBillingAddressId(1);

        OrderDTO result = orderService.createOrder(1, request);

        assertNotNull(result);
        assertEquals(7, variant.getStockQuantity());
        assertEquals(8, variant2.getStockQuantity());
        verify(productVariantRepository, times(2)).save(any(ProductVariant.class));
    }

    @Test
    void createOrder_variantNonAppartieneAlProdotto_LanciaEccezione() {
        Product otherProduct = new Product();
        otherProduct.setId(99);
        otherProduct.setName("OTHER");
        otherProduct.setPrice(new BigDecimal("50.00"));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(userAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(productRepository.findById(99)).thenReturn(Optional.of(otherProduct));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));

        CreateOrderRequest request = buildRequest(99, 1, 1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1, request));

        assertTrue(ex.getMessage().contains("does not belong to product"));
    }

    @Test
    void createOrder_userNonTrovato_LanciaEccezione() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        CreateOrderRequest request = buildRequest(1, 1, 1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(999, request));

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void createOrder_indirizzoMancante_LanciaEccezione() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        CreateOrderRequest request = buildRequest(1, 1, 1);
        request.setShippingAddressId(null);
        request.setBillingAddressId(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1, request));

        assertTrue(ex.getMessage().contains("address"));
    }
}
