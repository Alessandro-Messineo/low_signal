package org.ecommerce.java.low_signal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ecommerce.java.low_signal.model.Order;

public class OrderDTO {

    private Integer id;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal shippingPrice;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private String paymentStatus;
    private Integer userId;
    private String userEmail;
    private String userName;
    private AddressDTO shippingAddress;
    private AddressDTO billingAddress;
    private List<OrderItemDTO> orderItems;

    public static OrderDTO fromEntity(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.id = order.getId();
        dto.orderNumber = order.getOrderNumber();
        dto.subtotal = order.getSubtotal();
        dto.shippingPrice = order.getShippingPrice();
        dto.totalPrice = order.getTotalPrice();
        dto.createdAt = order.getCreatedAt();
        dto.paymentStatus = order.getPaymentStatus().name();

        if (order.getUser() != null) {
            dto.userId = order.getUser().getId();
            dto.userEmail = order.getUser().getEmail();
            dto.userName = order.getUser().getName() + " " + order.getUser().getSurname();
        }

        if (order.getShippingAddress() != null) {
            dto.shippingAddress = AddressDTO.fromEntity(order.getShippingAddress());
        }

        if (order.getBillingAddress() != null) {
            dto.billingAddress = AddressDTO.fromEntity(order.getBillingAddress());
        }

        if (order.getOrderItems() != null) {
            dto.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            dto.orderItems = Collections.emptyList();
        }

        return dto;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer value) {
        this.id = value;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public void setOrderNumber(String value) {
        this.orderNumber = value;
    }

    public BigDecimal getSubtotal() {
        return this.subtotal;
    }

    public void setSubtotal(BigDecimal value) {
        this.subtotal = value;
    }

    public BigDecimal getShippingPrice() {
        return this.shippingPrice;
    }

    public void setShippingPrice(BigDecimal value) {
        this.shippingPrice = value;
    }

    public BigDecimal getTotalPrice() {
        return this.totalPrice;
    }

    public void setTotalPrice(BigDecimal value) {
        this.totalPrice = value;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime value) {
        this.createdAt = value;
    }

    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    public void setPaymentStatus(String value) {
        this.paymentStatus = value;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer value) {
        this.userId = value;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public void setUserEmail(String value) {
        this.userEmail = value;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public AddressDTO getShippingAddress() {
        return this.shippingAddress;
    }

    public void setShippingAddress(AddressDTO value) {
        this.shippingAddress = value;
    }

    public AddressDTO getBillingAddress() {
        return this.billingAddress;
    }

    public void setBillingAddress(AddressDTO value) {
        this.billingAddress = value;
    }

    public List<OrderItemDTO> getOrderItems() {
        return this.orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> value) {
        this.orderItems = value;
    }

    public static class AddressDTO {
        private String fullName;
        private String street;
        private String city;
        private String postalCode;
        private String province;
        private String country;

        public static AddressDTO fromEntity(org.ecommerce.java.low_signal.model.Address address) {
            AddressDTO dto = new AddressDTO();
            dto.fullName = address.getFullName();
            dto.street = address.getStreet();
            dto.city = address.getCity();
            dto.postalCode = address.getPostalCode();
            dto.province = address.getProvince();
            dto.country = address.getCountry();
            return dto;
        }

        public String getFullName() {
            return this.fullName;
        }

        public void setFullName(String value) {
            this.fullName = value;
        }

        public String getStreet() {
            return this.street;
        }

        public void setStreet(String value) {
            this.street = value;
        }

        public String getCity() {
            return this.city;
        }

        public void setCity(String value) {
            this.city = value;
        }

        public String getPostalCode() {
            return this.postalCode;
        }

        public void setPostalCode(String value) {
            this.postalCode = value;
        }

        public String getProvince() {
            return this.province;
        }

        public void setProvince(String value) {
            this.province = value;
        }

        public String getCountry() {
            return this.country;
        }

        public void setCountry(String value) {
            this.country = value;
        }
    }
}
