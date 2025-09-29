package com.backoffice.fitandflex.exception;

import com.backoffice.fitandflex.dto.CommonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para proporcionar respuestas de error detalladas
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja errores de acceso denegado (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonDto.SuccessResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonDto.SuccessResponse.builder()
                        .success(false)
                        .message("Acceso denegado")
                        .data(new ErrorDetail("ACCESS_DENIED", ex.getMessage(), "Verifica que tengas los permisos necesarios"))
                        .build());
    }

    /**
     * Maneja errores de autenticación (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonDto.SuccessResponse<Object>> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonDto.SuccessResponse.builder()
                        .success(false)
                        .message("Error de autenticación")
                        .data(new ErrorDetail("AUTHENTICATION_FAILED", ex.getMessage(), "Token inválido o expirado"))
                        .build());
    }

    /**
     * Maneja credenciales incorrectas
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonDto.SuccessResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonDto.SuccessResponse.builder()
                        .success(false)
                        .message("Credenciales incorrectas")
                        .data(new ErrorDetail("BAD_CREDENTIALS", ex.getMessage(), "Email o contraseña incorrectos"))
                        .build());
    }

    /**
     * Maneja errores de validación
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonDto.SuccessResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonDto.SuccessResponse.builder()
                        .success(false)
                        .message("Error de validación")
                        .data(new ErrorDetail("VALIDATION_ERROR", ex.getMessage(), "Verifica los datos enviados"))
                        .build());
    }

    /**
     * Maneja errores generales
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonDto.SuccessResponse<Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonDto.SuccessResponse.builder()
                        .success(false)
                        .message("Error interno del servidor")
                        .data(new ErrorDetail("INTERNAL_ERROR", ex.getMessage(), "Contacta al administrador"))
                        .build());
    }

    /**
     * Clase para detalles de error
     */
    public static class ErrorDetail {
        private String code;
        private String message;
        private String suggestion;

        public ErrorDetail(String code, String message, String suggestion) {
            this.code = code;
            this.message = message;
            this.suggestion = suggestion;
        }

        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getSuggestion() { return suggestion; }
    }
}
