package org.ecommerce.java.low_signal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "product_images")
public class ProductImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank(message = "Enter url image")
  @Size(max = 500)
  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @NotBlank(message = "Enter alternative text")
  @Size(max = 100)
  @Column(name = "alt_text", nullable = false, length = 100)
  private String altText;

  @Column(name = "is_primary", nullable = false)
  private boolean isPrimary = false;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  public ProductImage() {
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer value) {
    this.id = value;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public void setImageUrl(String value) {
    this.imageUrl = value;
  }

  public String getAltText() {
    return this.altText;
  }

  public void setAltText(String value) {
    this.altText = value;
  }

  public boolean getIsPrimary() {
    return this.isPrimary;
  }

  public void setIsPrimary(boolean value) {
    this.isPrimary = value;
  }

  public Product getProduct() {
    return this.product;
  }

  public void setProduct(Product value) {
    this.product = value;
  }
}
