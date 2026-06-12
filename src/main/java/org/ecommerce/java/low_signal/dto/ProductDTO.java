package org.ecommerce.java.low_signal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.ecommerce.java.low_signal.model.Product;
import org.ecommerce.java.low_signal.model.ProductImage;
import org.ecommerce.java.low_signal.model.ProductVariant;

public class ProductDTO {

  private Integer id;
  private String name;
  private String slug;
  private String tag;
  private String description;
  private BigDecimal price;
  private BigDecimal comparePrice;
  private boolean featured;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Integer collectionId;
  private String collectionName;
  private List<ProductImage> images;
  private List<ProductVariant> variants;

  public static ProductDTO fromEntity(Product product) {
    ProductDTO dto = new ProductDTO();
    dto.id = product.getId();
    dto.name = product.getName();
    dto.slug = product.getSlug();
    dto.tag = product.getTag();
    dto.description = product.getDescription();
    dto.price = product.getPrice();
    dto.comparePrice = product.getComparePrice();
    dto.featured = product.getFeatured();
    dto.createdAt = product.getCreatedAt();
    dto.updatedAt = product.getUpdatedAt();
    dto.collectionId = product.getCollection().getId();
    dto.collectionName = product.getCollection().getName();
    dto.images = product.getImages();
    dto.variants = product.getVariants();
    return dto;
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

  public String getTag() {
    return this.tag;
  }

  public void setTag(String value) {
    this.tag = value;
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

  public Integer getCollectionId() {
    return this.collectionId;
  }

  public void setCollectionId(Integer value) {
    this.collectionId = value;
  }

  public List<ProductImage> getImages() {
    return this.images;
  }

  public void setImages(List<ProductImage> value) {
    this.images = value;
  }

  public List<ProductVariant> getVariants() {
    return this.variants;
  }

  public void setVariants(List<ProductVariant> value) {
    this.variants = value;
  }

  public String getCollectionName() {
    return this.collectionName;
  }

  public void setCollectionName(String value) {
    this.collectionName = value;
  }
}