package org.ecommerce.java.low_signal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank(message = "Enter name of product")
  @Size(max = 100)
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank(message = "Enter slug of product")
  @Size(max = 100)
  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @NotBlank(message = "Enter description of product")
  @Size(max = 500)
  @Column(nullable = false, length = 500)
  private String description;

  @NotNull(message = "Enter price")
  @DecimalMin(value = "1.00", message = "Price must be at least 1")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @NotNull(message = "Enter compare price")
  @DecimalMin(value = "1.00", message = "Compare price must be at least 1")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal comparePrice;

  @Column(nullable = false)
  private boolean featured = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "collection_id", nullable = false)
  private Collection collection;

  public Product() {
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer value) {
    this.id = value;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getSlug() {
    return this.slug;
  }

  public void setSlug(String value) {
    this.slug = value;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public BigDecimal getPrice() {
    return this.price;
  }

  public void setPrice(BigDecimal value) {
    this.price = value;
  }

  public BigDecimal getComparePrice() {
    return this.comparePrice;
  }

  public void setComparePrice(BigDecimal value) {
    this.comparePrice = value;
  }

  public boolean getFeatured() {
    return this.featured;
  }

  public void setFeatured(boolean value) {
    this.featured = value;
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

  public Collection getCollection() {
    return this.collection;
  }

  public void setCollection(Collection value) {
    this.collection = value;
  }
}
