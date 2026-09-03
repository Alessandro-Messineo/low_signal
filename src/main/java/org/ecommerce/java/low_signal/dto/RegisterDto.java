package org.ecommerce.java.low_signal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 20)
    private String name;

    @NotBlank(message = "Surname cannot be empty")
    @Size(max = 20)
    private String surname;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Enter a valid email")
    @Size(max = 50)
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100)
    @Pattern(
        regexp = "^(?=.*\\d)(?=.*[A-Z])(?=.*[\\W_]).{6,}$",
        message = "Password must contain at least one uppercase letter, one digit, and one special character"
    )
    private String password;

    @NotBlank(message = "Phone cannot be empty")
    @Size(min = 10, max = 10, message = "Phone must be exactly 10 characters")
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
