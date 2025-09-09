package com.dgarciacasam.auth_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.dgarciacasam.authService.exceptions.UserAlreadyExistsException;
import com.dgarciacasam.authService.models.LoginRequest;
import com.dgarciacasam.authService.models.RegisterRequest;
import com.dgarciacasam.authService.models.UserResponse;
import com.dgarciacasam.authService.models.entity.User;
import com.dgarciacasam.authService.repositories.AuthRepository;
import com.dgarciacasam.authService.services.AuthService;
import com.dgarciacasam.authService.services.JwtService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthRepository authRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password123", "test@example.com");
        loginRequest = new LoginRequest("testuser", "password123");
        mockUser = new User("testuser", "encodedPassword", "test@example.com");
        mockUser.setId(1L);
    }

    @Test
    void testRegister_Successful() {
        when(authRepository.existsByUsername("testuser")).thenReturn(false);
        when(authRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(authRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken("testuser")).thenReturn("mocked-token");

        UserResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mocked-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        verify(authRepository).save(any(User.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        when(authRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        verify(authRepository, never()).save(any());
    }

    @Test
    void testLogin_Successful() {
        Authentication mockAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(mockAuth.getName()).thenReturn("testuser");
        when(jwtService.generateToken("testuser")).thenReturn("mocked-token");

        UserResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void testLogin_InvalidCredentials_ExceptionThrown() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }
}
