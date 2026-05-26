package org.ecommerce.java.low_signal.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "The name cannot be empty")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String name;

    @NotBlank(message = "The surname cannot be empty")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String surname;

    @NotBlank(message = "The email cannot be empty")
    @Email(message = "Enter a valid email")
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 5, max = 100)
    @Column(nullable = false, length = 100)
    private String password;

    @NotBlank(message = "Phone cannot be empty")
    @Size(min = 10, max = 10, message = "Phone must be exactly 10 characters")
    @Column(nullable = false, length = 10)
    private String phone;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User() {
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

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String value) {
        this.surname = value;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
