package com.dgarciacasam.authService.Models;

import lombok.Data;

@Data
public class UserResponse {
    private String token;
    private String refreshToken;
    private String username;

    public UserResponse(String token, String refreshToken, String username) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
    }

}
