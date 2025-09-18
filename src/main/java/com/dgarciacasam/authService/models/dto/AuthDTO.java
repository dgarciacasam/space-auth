package com.dgarciacasam.authService.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDTO {
    UserDTO user;
    String jwt;
    String refreshToken;
}
