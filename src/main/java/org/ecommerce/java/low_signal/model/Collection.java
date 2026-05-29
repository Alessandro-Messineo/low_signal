package org.ecommerce.java.low_signal.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "collections")
public class Collection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank(message = "Enter name of collection")
  @Size(max = 100)
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank(message = "Enter slug of collection")
  @Size(max = 100)
  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @NotBlank(message = "Enter description of collection")
  @Size(max = 500)
  @Column(nullable = false, length = 500)
  private String description;

  @NotBlank(message = "Enter season of collection")
  @Size(max = 10)
  @Column(nullable = false, length = 10)
  private String season;

  @NotBlank(message = "Enter url image")
  @Size(max = 500)
  @Column(name = "hero_image", nullable = false, length = 500)
  private String heroImage;

  @Column(nullable = false)
  private boolean featured = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "collection_status", nullable = false)
  private CollectionStatus collectionStatus;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @JsonManagedReference
  @OneToMany(mappedBy = "collection")
  private List<Product> products;

  public Collection() {
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

  public String getHeroImage() {
    return this.heroImage;
  }

  public void setHeroImage(String value) {
    this.heroImage = value;
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

  public List<Product> getProducts() {
    return this.products;
  }

  public void setProducts(List<Product> value) {
    this.products = value;
  }

  public String getSeason() {
    return this.season;
  }

  public void setSeason(String value) {
    this.season = value;
  }

  public CollectionStatus getCollectionStatus() {
    return this.collectionStatus;
  }

  public void setCollectionStatus(CollectionStatus value) {
    this.collectionStatus = value;
  }
}
