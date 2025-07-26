package com.dgarciacasam.authService.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dgarciacasam.authService.Exceptions.UserAlreadyExistsException;
import com.dgarciacasam.authService.Models.LoginRequest;
import com.dgarciacasam.authService.Models.RegisterRequest;
import com.dgarciacasam.authService.Models.User;
import com.dgarciacasam.authService.Models.UserResponse;
import com.dgarciacasam.authService.Repositories.AuthRepository;

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
        if (authRepository.existsByUsername(registerDTO.getUsername()) || authRepository.existsByEmail(registerDTO.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        log.info("Register successful");

        User newUser = new User(registerDTO.getUsername(), encoder.encode(registerDTO.getPassword()), registerDTO.getEmail());
        User registeredUser = authRepository.save(newUser);
        String generatedToken = jwtService.generateToken(registeredUser.getUsername());
        UserResponse response = new UserResponse(generatedToken,null, registeredUser.getUsername());
        return response;
    }

    public UserResponse login(LoginRequest loginDTO) {
        log.info("Login request: {}", loginDTO);
        
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
        }

        log.info("Login successful");
        String generatedToken = jwtService.generateToken(authentication.getName());
        UserResponse response = new UserResponse(generatedToken, null, authentication.getName());
        return response;
    }
}
