
package com.dgarciacasam.authService.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dgarciacasam.authService.services.AuthService;
import com.dgarciacasam.authService.models.LoginRequest;
import com.dgarciacasam.authService.models.RegisterRequest;
import com.dgarciacasam.authService.models.UserRes;
import com.dgarciacasam.authService.models.UserResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserRes> register(@Valid @RequestBody RegisterRequest registerRequest,
            HttpServletResponse httpRes) {
        log.info("Register request: {}", registerRequest);
        UserResponse response = authService.register(registerRequest);
        UserRes res = new UserRes(response.getId(), response.getUsername());
        // Generate cookie with jwt
        Cookie cookie = generateJwtCookie("jwt", response.getToken());
        httpRes.addCookie(cookie);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<UserRes> login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse httpRes) {
        log.info("Login request: {}", loginRequest);
        UserResponse response = authService.login(loginRequest);
        UserRes res = new UserRes(response.getId(), response.getUsername());
        Cookie cookie = generateJwtCookie("jwt", response.getToken());
        Cookie refreshCookie = generateJwtCookie("refreshToken", response.getRefreshToken());
        httpRes.addCookie(cookie);
        httpRes.addCookie(refreshCookie);

        return ResponseEntity.ok(res);
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

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = true) String refreshToken,
            HttpServletResponse httpRes) {
        log.info("Refresh request with refreshToken={}", refreshToken);

        String generatedToken = authService.refresh(refreshToken);

        // Generate cookie with jwt
        Cookie cookie = generateJwtCookie("jwt", generatedToken);
        httpRes.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserResponse> verify(@CookieValue(name = "jwt", required = false) String jwtCookie,
            HttpServletRequest request,
            HttpServletResponse httpRes) {
        log.info("Verifying jwt");
        System.out.println(jwtCookie);
        if (jwtCookie == null || jwtCookie.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        UserResponse response = authService.verify(jwtCookie);

        // Generate new cookie with the same token if it's still valid
        Cookie cookie = generateJwtCookie("jwt", response.getToken());
        httpRes.addCookie(cookie);

        return ResponseEntity.ok(response);
    }

    private Cookie generateJwtCookie(String tokenName, String token) {
        Cookie cookie = new Cookie(tokenName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600);

        return cookie;
    }

}
