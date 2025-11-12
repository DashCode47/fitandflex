package com.backoffice.fitandflex.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    
    @NotBlank(message = "El token es obligatorio")
    private String token;
}

