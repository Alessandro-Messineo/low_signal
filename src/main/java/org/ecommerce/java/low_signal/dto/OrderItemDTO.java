package org.ecommerce.java.low_signal.dto;

import java.math.BigDecimal;

import org.ecommerce.java.low_signal.model.OrderItem;

public class OrderItemDTO {

    private Integer id;
    private Integer quantity;
    private String productName;
    private BigDecimal productPrice;
    private String size;
    private Integer productId;
    private String productSlug;
    private String productTag;
    private Integer productVariantId;
    private String sku;

    public static OrderItemDTO fromEntity(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.id = item.getId();
        dto.quantity = item.getQuantity();
        dto.productName = item.getProductName();
        dto.productPrice = item.getProductPrice();
        dto.size = item.getSize();

        if (item.getProduct() != null) {
            dto.productId = item.getProduct().getId();
            dto.productSlug = item.getProduct().getSlug();
            dto.productTag = item.getProduct().getTag();
        }

        if (item.getProductVariant() != null) {
            dto.productVariantId = item.getProductVariant().getId();
            dto.sku = item.getProductVariant().getSku();
        }

        return dto;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer value) {
        this.id = value;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer value) {
        this.quantity = value;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String value) {
        this.productName = value;
    }

    public BigDecimal getProductPrice() {
        return this.productPrice;
    }

    public void setProductPrice(BigDecimal value) {
        this.productPrice = value;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String value) {
        this.size = value;
    }

    public Integer getProductId() {
        return this.productId;
    }

    public void setProductId(Integer value) {
        this.productId = value;
    }

    public String getProductSlug() {
        return this.productSlug;
    }

    public void setProductSlug(String value) {
        this.productSlug = value;
    }

    public String getProductTag() {
        return this.productTag;
    }

    public void setProductTag(String value) {
        this.productTag = value;
    }

    public Integer getProductVariantId() {
        return this.productVariantId;
    }

    public void setProductVariantId(Integer value) {
        this.productVariantId = value;
    }

    public String getSku() {
        return this.sku;
    }

    public void setSku(String value) {
        this.sku = value;
    }
}
