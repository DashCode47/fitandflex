package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.AuthRequest;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.entity.Role;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.UserRepository;
import com.backoffice.fitandflex.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;
    private Branch testBranch;
    private AuthRequest validAuthRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("USER");

        testBranch = new Branch();
        testBranch.setId(1L);
        testBranch.setName("Sucursal Test");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword("encodedPassword");
        testUser.setActive(true);
        testUser.setRole(testRole);
        testUser.setBranch(testBranch);

        validAuthRequest = new AuthRequest();
        validAuthRequest.setEmail("test@example.com");
        validAuthRequest.setPassword("password123");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        Authentication mockAuth = mock(Authentication.class);
        UserDetails mockUserDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(mockUserDetails);
        when(mockUserDetails.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(), any())).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.branchId").value(1))
                .andExpect(jsonPath("$.branchName").value("Sucursal Test"))
                .andExpect(jsonPath("$.active").value(true));

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any(), any());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_WithInactiveUser_ShouldReturn401() throws Exception {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_WithNonExistentUser_ShouldReturn401() throws Exception {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Arrange
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithEmptyPassword_ShouldReturn400() throws Exception {
        // Arrange
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnUserInfo() throws Exception {
        // Arrange
        String validToken = "Bearer valid-jwt-token";
        when(jwtService.extractUsername("valid-jwt-token")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(jwtService).extractUsername("valid-jwt-token");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturn401() throws Exception {
        // Arrange
        String invalidToken = "Bearer invalid-token";
        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void validateToken_WithMissingHeader_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void logout_ShouldReturnSuccessMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente"));
    }
}
