package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.AuthRequest;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.entity.Role;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.BranchRepository;
import com.backoffice.fitandflex.repository.RoleRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Role testRole;
    private Branch testBranch;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        userRepository.deleteAll();
        roleRepository.deleteAll();
        branchRepository.deleteAll();

        // Create test role
        testRole = new Role();
        testRole.setName("USER");
        testRole = roleRepository.save(testRole);

        // Create test branch
        testBranch = new Branch();
        testBranch.setName("Test Branch");
        testBranch.setAddress("Test Address");
        testBranch.setCity("Test City");
        testBranch.setState("Test State");
        testBranch.setCountry("Test Country");
        testBranch.setPhone("123456789");
        testBranch.setEmail("test@branch.com");
        testBranch = branchRepository.save(testBranch);

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setPhone("123456789");
        testUser.setGender("M");
        testUser.setActive(true);
        testUser.setRole(testRole);
        testUser.setBranch(testBranch);
        testUser = userRepository.save(testUser);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.branchId").value(testBranch.getId()))
                .andExpect(jsonPath("$.branchName").value("Test Branch"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    @Test
    void login_WithInvalidPassword_ShouldReturn401() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void login_WithNonExistentUser_ShouldReturn401() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void login_WithInactiveUser_ShouldReturn401() throws Exception {
        // Arrange
        testUser.setActive(false);
        userRepository.save(testUser);

        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void login_WithInvalidEmailFormat_ShouldReturn400() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithEmptyFields_ShouldReturn400() throws Exception {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("");
        request.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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
