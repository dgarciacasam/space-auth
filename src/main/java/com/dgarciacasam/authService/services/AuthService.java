package com.dgarciacasam.authService.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dgarciacasam.authService.exceptions.UserAlreadyExistsException;
import com.dgarciacasam.authService.models.LoginRequest;
import com.dgarciacasam.authService.models.RegisterRequest;
import com.dgarciacasam.authService.models.UserPrincipal;
import com.dgarciacasam.authService.models.UserResponse;
import com.dgarciacasam.authService.models.entity.User;
import com.dgarciacasam.authService.repositories.AuthRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private JwtService jwtService;

    public UserResponse register(RegisterRequest registerDTO) {
        log.info("Register request: {}", registerDTO);
        if (authRepository.existsByUsername(registerDTO.getUsername())
                || authRepository.existsByEmail(registerDTO.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        log.info("Register successful");

        User newUser = new User(registerDTO.getUsername(), encoder.encode(registerDTO.getPassword()),
                registerDTO.getEmail());
        User registeredUser = authRepository.save(newUser);
        String generatedToken = jwtService.generateToken(registeredUser.getUsername());
        String refreshToken = jwtService.generateToken(registeredUser.getUsername());
        UserResponse response = new UserResponse(registeredUser.getId(), registeredUser.getUsername(), generatedToken,
                refreshToken);
        return response;
    }

    public UserResponse login(LoginRequest loginDTO) {
        log.info("Login request: {}", loginDTO);

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
        }

        log.info("Login successful");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String generatedToken = jwtService.generateToken(authentication.getName());
        String refreshToken = jwtService.generateToken(authentication.getName());
        UserResponse response = new UserResponse(userPrincipal.getId(), userPrincipal.getUsername(), generatedToken,
                refreshToken);
        return response;
    }

    public String refresh(String refreshToken) {
        log.info("Refresh request");

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(refreshToken, null));
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect refresh token");
        }

        log.info("Refresh successful");
        String generatedToken = jwtService.generateToken(authentication.getName());
        return generatedToken;
    }

    public UserResponse verify(String token) {
        log.info("Verifying jwt");
        UserResponse response = new UserResponse(null, null, token, null);
        return response;
    }
}
