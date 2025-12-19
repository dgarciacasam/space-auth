package com.space.authService.auth_service;

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

import com.space.auth.exceptions.UserAlreadyExistsException;
import com.space.auth.models.LoginRequest;
import com.space.auth.models.RegisterRequest;
import com.space.auth.models.UserPrincipal;
import com.space.auth.models.dto.AuthDTO;
import com.space.auth.models.entity.User;
import com.space.auth.repositories.AuthRepository;
import com.space.auth.services.AuthService;
import com.space.auth.services.JwtService;

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

        AuthDTO response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mocked-token", response.getJwt());
        assertEquals("testuser", response.getUser().getUsername());
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
        UserPrincipal mockUserPrincipal = new UserPrincipal(mockUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(mockAuth.getName()).thenReturn("testuser");
        when(mockAuth.getPrincipal()).thenReturn(mockUserPrincipal);
        when(jwtService.generateToken("testuser")).thenReturn("mocked-token");

        AuthDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-token", response.getJwt());
        assertEquals("testuser", response.getUser().getUsername());
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
