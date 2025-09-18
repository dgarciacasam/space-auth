package com.dgarciacasam.authService.models;

import com.dgarciacasam.authService.models.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRes {
    private UserDTO user;
    private String jwt;
}
