package com.dgarciacasam.authService.models;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String token;
    private String refreshToken;

    public UserResponse(Long id, String username, String token, String refreshToken) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.refreshToken = refreshToken;
    }

}
