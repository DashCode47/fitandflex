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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
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
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        // load user entity to include userId and branch in response/claims
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", userDetails.getAuthorities().stream().map(Object::toString).toList());
        extraClaims.put("userId", user.getId());
        extraClaims.put("branchId", user.getBranch() != null ? user.getBranch().getId() : null);

        String jwt = jwtService.generateToken(extraClaims, userDetails);

        AuthResponse resp = AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .expiresIn(null) // puedes calcular y retornar expiration si quieres
                .userId(user.getId())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .build();

        return ResponseEntity.ok(resp);
    }
}
