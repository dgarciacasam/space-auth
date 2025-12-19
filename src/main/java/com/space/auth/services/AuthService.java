package com.space.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.space.auth.exceptions.UserAlreadyExistsException;
import com.space.auth.models.LoginRequest;
import com.space.auth.models.RegisterRequest;
import com.space.auth.models.UserPrincipal;
import com.space.auth.models.dto.AuthDTO;
import com.space.auth.models.dto.UserDTO;
import com.space.auth.models.entity.User;
import com.space.auth.repositories.AuthRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {
    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthDTO register(RegisterRequest registerDTO) {
        log.info("Register request: {}", registerDTO);
        if (authRepository.existsByUsername(registerDTO.getUsername())
                || authRepository.existsByEmail(registerDTO.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        log.info("Register successful");

        User newUser = new User(registerDTO.getUsername(), passwordEncoder.encode(registerDTO.getPassword()),
                registerDTO.getEmail());
        User registeredUser = authRepository.save(newUser);

        String generatedToken = jwtService.generateToken(registeredUser.getUsername());
        String refreshToken = jwtService.generateToken(registeredUser.getUsername());

        UserDTO user = new UserDTO(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getEmail());
        AuthDTO response = new AuthDTO(user, generatedToken, refreshToken);
        return response;
    }

    public AuthDTO login(LoginRequest loginDTO) {
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
        UserDTO user = new UserDTO(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail());
        AuthDTO response = new AuthDTO(user, generatedToken, refreshToken);
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

    public AuthDTO verify(String token) {
        log.info("Verifying jwt");
        AuthDTO response = new AuthDTO(null, token, null);
        return response;
    }
}
