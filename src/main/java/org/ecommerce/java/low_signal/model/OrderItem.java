package org.ecommerce.java.low_signal.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Enter quantity")
    @Min(value = 1)
    @Column(nullable = false)
    private Integer quantity;

    @NotBlank(message = "Enter name of product")
    @Size(max = 100)
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @NotNull(message = "Enter price")
    @DecimalMin(value = "1.00", message = "Price must be at least 1")
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;

    @NotBlank(message = "Enter size of product")
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String size;

    // aggiungere product_id per la relazione

    // aggiungere order_id per la relazione

    // aggiungere product_variant_id per la relazione

    public OrderItem() {
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
}
