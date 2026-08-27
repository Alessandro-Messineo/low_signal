package org.ecommerce.java.low_signal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ecommerce.java.low_signal.dto.CreateOrderRequest;
import org.ecommerce.java.low_signal.dto.OrderDTO;
import org.ecommerce.java.low_signal.model.Address;
import org.ecommerce.java.low_signal.model.Order;
import org.ecommerce.java.low_signal.model.OrderItem;
import org.ecommerce.java.low_signal.model.PaymentStatus;
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
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserAddressRepository userAddressRepository;
    private final AddressRepository addressRepository;

    public OrderService(OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            UserAddressRepository userAddressRepository,
            AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userAddressRepository = userAddressRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllWithDetails()
                .stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrderDTO> getMyOrders(Integer userId) {
        return orderRepository.findAllByUserIdWithDetails(userId)
                .stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO createOrder(Integer userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address shippingAddress = resolveAddress(userId, request.getShippingAddressId(),
                request.getShippingAddress());
        Address billingAddress = resolveAddress(userId, request.getBillingAddressId(),
                request.getBillingAddress());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with id: " + itemRequest.getProductId()));

            ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product variant not found with id: " + itemRequest.getProductVariantId()));

            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new RuntimeException("Variant " + variant.getId()
                        + " does not belong to product " + product.getId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductVariant(variant);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setSize(variant.getSize());
            orderItem.setQuantity(itemRequest.getQuantity());

            orderItems.add(orderItem);

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        BigDecimal shippingPrice = BigDecimal.ZERO;
        BigDecimal totalPrice = subtotal.add(shippingPrice);

        String orderNumber = "LS-" + System.currentTimeMillis();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setSubtotal(subtotal);
        order.setShippingPrice(shippingPrice);
        order.setTotalPrice(totalPrice);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        return OrderDTO.fromEntity(savedOrder);
    }

    private Address resolveAddress(Integer userId, Integer addressId,
            CreateOrderRequest.AddressRequest inlineAddress) {
        if (addressId != null) {
            UserAddress userAddress = userAddressRepository.findByIdAndUserId(addressId, userId)
                    .orElseThrow(() -> new RuntimeException(
                            "Address not found or does not belong to user"));

            Address address = new Address();
            address.setFullName(userAddress.getFullName());
            address.setStreet(userAddress.getStreet());
            address.setCity(userAddress.getCity());
            address.setPostalCode(userAddress.getPostalCode());
            address.setProvince(userAddress.getProvince());
            address.setCountry(userAddress.getCountry());
            return addressRepository.save(address);
        }

        if (inlineAddress != null) {
            Address address = new Address();
            address.setFullName(inlineAddress.getFullName());
            address.setStreet(inlineAddress.getStreet());
            address.setCity(inlineAddress.getCity());
            address.setPostalCode(inlineAddress.getPostalCode());
            address.setProvince(inlineAddress.getProvince());
            address.setCountry(inlineAddress.getCountry());
            return addressRepository.save(address);
        }

        throw new RuntimeException("Shipping address is required: provide addressId or inline address");
    }
}
