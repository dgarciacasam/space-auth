
package com.space.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.space.auth.models.AuthRes;
import com.space.auth.models.LoginRequest;
import com.space.auth.models.RegisterRequest;
import com.space.auth.models.dto.AuthDTO;
import com.space.auth.services.AuthService;

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
    public ResponseEntity<AuthRes> register(@Valid @RequestBody RegisterRequest registerRequest,
            HttpServletResponse httpRes) {
        log.info("Register request: {}", registerRequest);
        AuthDTO auth = authService.register(registerRequest);
        AuthRes res = new AuthRes(auth.getUser(), auth.getJwt());

        // Generate cookie with jwt
        Cookie cookie = generateJwtCookie("refreshToken", auth.getRefreshToken());
        httpRes.addCookie(cookie);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse httpRes) {
        log.info("Login request: {}", loginRequest);

        AuthDTO auth = authService.login(loginRequest);

        Cookie refreshCookie = generateJwtCookie("refreshToken", auth.getRefreshToken());
        AuthRes res = new AuthRes(auth.getUser(), auth.getJwt());

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
    public ResponseEntity<AuthRes> verify(@CookieValue(name = "jwt", required = false) String jwtCookie,
            HttpServletRequest request,
            HttpServletResponse httpRes) {
        log.info("Verifying jwt");
        System.out.println(jwtCookie);
        if (jwtCookie == null || jwtCookie.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        AuthDTO response = authService.verify(jwtCookie);

        AuthRes res = new AuthRes(response.getUser(), response.getJwt());
        // Generate new cookie with the same token if it's still valid
        Cookie cookie = generateJwtCookie("jwt", res.getJwt());
        httpRes.addCookie(cookie);

        return ResponseEntity.ok(res);
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
