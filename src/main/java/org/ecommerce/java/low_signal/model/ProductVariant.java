package org.ecommerce.java.low_signal.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank(message = "Enter size of product")
  @Size(max = 10)
  @Column(nullable = false, length = 10)
  private String size;

  @NotNull(message = "Enter quantity")
  @Min(value = 0)
  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity;

  @NotBlank(message = "Enter sku of product")
  @Size(max = 100)
  @Column(nullable = false, unique = true, length = 100)
  private String sku;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @JsonIgnore
  @OneToMany(mappedBy = "productVariant", fetch = FetchType.LAZY)
  private List<OrderItem> orderItems;

  public ProductVariant() {
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer value) {
    this.id = value;
  }

  public String getSize() {
    return this.size;
  }

  public void setSize(String value) {
    this.size = value;
  }

  public Integer getStockQuantity() {
    return this.stockQuantity;
  }

  public void setStockQuantity(Integer value) {
    this.stockQuantity = value;
  }

  public String getSku() {
    return this.sku;
  }

  public void setSku(String value) {
    this.sku = value;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime value) {
    this.createdAt = value;
  }

  public LocalDateTime getUpdatedAt() {
    return this.updatedAt;
  }

  public void setUpdatedAt(LocalDateTime value) {
    this.updatedAt = value;
  }

  public Product getProduct() {
    return this.product;
  }

  public void setProduct(Product value) {
    this.product = value;
  }

  public List<OrderItem> getOrderItems() {
    return this.orderItems;
  }

  public void setOrderItems(List<OrderItem> value) {
    this.orderItems = value;
  }
}
