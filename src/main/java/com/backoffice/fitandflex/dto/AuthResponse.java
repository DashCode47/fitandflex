package com.backoffice.fitandflex.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType; // "Bearer"
    private Long expiresIn;
    private Long userId;
    private String role;
    private Long branchId;
}
