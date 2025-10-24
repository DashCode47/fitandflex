package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.AuthRequest;
import com.backoffice.fitandflex.dto.AuthResponse;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.UserRepository;
import com.backoffice.fitandflex.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints para autenticación de usuarios")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario y retorna un JWT token para acceder a los endpoints protegidos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            log.info("Intento de login para usuario: {}", request.getEmail());
            
            // Verificar si el usuario existe y está activo
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
            
            if (!user.getActive()) {
                log.warn("Intento de login con usuario inactivo: {}", request.getEmail());
                throw new DisabledException("Usuario inactivo");
            }

            // Autenticar usuario
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            // Preparar claims adicionales para el JWT
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("roles", userDetails.getAuthorities().stream().map(Object::toString).toList());
            extraClaims.put("userId", user.getId());
            extraClaims.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);
            extraClaims.put("userActive", user.getActive());

            // Generar JWT token
            String jwt = jwtService.generateToken(extraClaims, userDetails);

            // Calcular tiempo de expiración en segundos
            Long expiresIn = jwtService.getExpirationTime() / 1000; // convertir a segundos

            // Construir respuesta
            AuthResponse response = AuthResponse.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .expiresIn(expiresIn)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole() != null ? user.getRole().getName() : null)
                    .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                    .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                    .active(user.getActive())
                    .build();

            log.info("Login exitoso para usuario: {} (ID: {})", user.getEmail(), user.getId());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            log.warn("Error de autenticación para usuario: {} - {}", request.getEmail(), ex.getMessage());
            throw ex; // Re-lanzar para que el GlobalExceptionHandler lo maneje
        } catch (Exception ex) {
            log.error("Error inesperado durante login para usuario: {}", request.getEmail(), ex);
            throw new RuntimeException("Error interno del servidor", ex);
        }
    }

    @Operation(
        summary = "Validar token",
        description = "Valida si un JWT token es válido y retorna información del usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token válido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token inválido o expirado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new BadCredentialsException("Token de autorización inválido");
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

            if (!user.getActive()) {
                throw new DisabledException("Usuario inactivo");
            }

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime() / 1000)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole() != null ? user.getRole().getName() : null)
                    .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                    .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                    .active(user.getActive())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.warn("Error validando token: {}", ex.getMessage());
            throw new BadCredentialsException("Token inválido");
        }
    }

    @Operation(
        summary = "Cerrar sesión",
        description = "Endpoint para cerrar sesión (el token se invalida en el cliente)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sesión cerrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        log.info("Usuario cerró sesión");
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Sesión cerrada exitosamente"
        ));
    }
}
