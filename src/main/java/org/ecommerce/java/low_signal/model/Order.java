package org.ecommerce.java.low_signal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
 
    @NotBlank(message = "Enter order number")
    @Size(max = 100)
    @Column(name = "order_number", nullable = false,unique = true, length = 100)
    private String orderNumber;

    @NotNull(message = "Enter subtotal")
    @DecimalMin(value = "1.00", message = "Price must be at least 1")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotNull(message = "Enter shipping price")
    @DecimalMin(value = "0.00", message = "Shipping price must be at least 0")
    @Column(name = "shipping_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingPrice;

    @NotNull(message = "Enter total price")
    @DecimalMin(value = "1.00", message = "Price must be at least 1")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Order(){}

    
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
}
