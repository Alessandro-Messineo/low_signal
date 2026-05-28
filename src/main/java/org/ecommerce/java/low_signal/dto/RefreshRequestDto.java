package org.ecommerce.java.low_signal.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequestDto {

    @NotBlank(message = "Refresh token cannot be empty")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
