
package com.dgarciacasam.authService.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dgarciacasam.authService.Models.LoginRequest;
import com.dgarciacasam.authService.Models.RegisterRequest;
import com.dgarciacasam.authService.Models.UserResponse;
import com.dgarciacasam.authService.Services.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse httpRes) {
        log.info("Register request: {}", registerRequest);
        UserResponse response = authService.register(registerRequest);
        
        //Generate cookie with jwt
        Cookie cookie = generateJwtCookie(response.getToken());
        httpRes.addCookie(cookie);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse httpRes) {
        log.info("Login request: {}", loginRequest);
        UserResponse response = authService.login(loginRequest);

        //Generate cookie with jwt
        Cookie cookie = generateJwtCookie(response.getToken());
        httpRes.addCookie(cookie);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse httpRes) {
        log.info("Logout request");

        // Generate and empty cookie
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        // Add cookie to response
        httpRes.addCookie(cookie);
        
        return ResponseEntity.ok("Session closed");
    }

    private Cookie generateJwtCookie(String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600);

        return cookie;
    }

}
