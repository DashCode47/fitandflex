package com.backoffice.fitandflex.dto;

import lombok.*;

/**
 * DTOs comunes para respuestas de la API
 */
public class CommonDto {

    /**
     * Respuesta estándar de éxito
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuccessResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }

    /**
     * Respuesta de error
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {
        private boolean success;
        private String message;
        private String error;
        private int status;
    }

    /**
     * DTO para autenticación
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token;
        private String tokenType;
        private Long expiresIn;
        private Long userId;
        private String role;
        private Long branchId;
    }

    /**
     * DTO para login
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        private String email;
        private String password;
    }

    /**
     * DTO para registro
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String phone;
        private String gender;
        private Long roleId;
        private Long branchId;
    }

    /**
     * DTO para actualizar perfil
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateProfileRequest {
        private String name;
        private String phone;
        private String gender;
        private String profileImageUrl;
    }

    /**
     * DTO para cambiar contraseña
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    /**
     * DTO para recuperar contraseña
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForgotPasswordRequest {
        private String email;
    }

    /**
     * DTO para resetear contraseña
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
    }

    /**
     * DTO para verificar email
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerifyEmailRequest {
        private String token;
    }
}
