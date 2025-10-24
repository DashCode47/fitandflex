package com.backoffice.fitandflex.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType; // "Bearer"
    private Long expiresIn; // en segundos
    private Long userId;
    private String email;
    private String name;
    private String role;
    private Long branchId;
    private String branchName;
    private Boolean active;
}
