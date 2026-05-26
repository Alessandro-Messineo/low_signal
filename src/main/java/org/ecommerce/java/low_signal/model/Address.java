package org.ecommerce.java.low_signal.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Full name cannot be empty")
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Street cannot be empty")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String street;

    @NotBlank(message = "City cannot be empty")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "Postal code cannot be empty")
    @Size(max = 20)
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @NotBlank(message = "Province cannot be empty")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String province;

    @NotBlank(message = "Country cannot be empty")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String country;

    @OneToMany(mappedBy = "shippingAddress")
    private List<Order> shippingOrders;

    @OneToMany(mappedBy = "billingAddress")
    private List<Order> billingOrders;

    public Address() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer value) {
        this.id = value;
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

    public List<Order> getShippingOrders() {
        return this.shippingOrders;
    }

    public void setShippingOrders(List<Order> value) {
        this.shippingOrders = value;
    }

    public List<Order> getBillingOrders() {
        return this.billingOrders;
    }

    public void setBillingOrders(List<Order> value) {
        this.billingOrders = value;
    }
}