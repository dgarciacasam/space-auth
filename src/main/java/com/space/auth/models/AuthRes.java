package com.space.auth.models;

import com.space.auth.models.dto.UserDTO;

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
