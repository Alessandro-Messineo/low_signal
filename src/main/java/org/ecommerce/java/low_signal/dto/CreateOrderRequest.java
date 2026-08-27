package org.ecommerce.java.low_signal.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    private Integer shippingAddressId;
    private Integer billingAddressId;

    @Valid
    private AddressRequest shippingAddress;

    @Valid
    private AddressRequest billingAddress;

    public List<OrderItemRequest> getItems() {
        return this.items;
    }

    public void setItems(List<OrderItemRequest> value) {
        this.items = value;
    }

    public Integer getShippingAddressId() {
        return this.shippingAddressId;
    }

    public void setShippingAddressId(Integer value) {
        this.shippingAddressId = value;
    }

    public Integer getBillingAddressId() {
        return this.billingAddressId;
    }

    public void setBillingAddressId(Integer value) {
        this.billingAddressId = value;
    }

    public AddressRequest getShippingAddress() {
        return this.shippingAddress;
    }

    public void setShippingAddress(AddressRequest value) {
        this.shippingAddress = value;
    }

    public AddressRequest getBillingAddress() {
        return this.billingAddress;
    }

    public void setBillingAddress(AddressRequest value) {
        this.billingAddress = value;
    }

    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Integer productId;

        @NotNull(message = "Product variant ID is required")
        private Integer productVariantId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public Integer getProductId() {
            return this.productId;
        }

        public void setProductId(Integer value) {
            this.productId = value;
        }

        public Integer getProductVariantId() {
            return this.productVariantId;
        }

        public void setProductVariantId(Integer value) {
            this.productVariantId = value;
        }

        public Integer getQuantity() {
            return this.quantity;
        }

        public void setQuantity(Integer value) {
            this.quantity = value;
        }
    }

    public static class AddressRequest {

        private String fullName;
        private String street;
        private String city;
        private String postalCode;
        private String province;
        private String country;

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
