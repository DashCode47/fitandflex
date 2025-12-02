package com.backoffice.fitandflex.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para producción.
 * Evita exponer stack traces y detalles internos a los usuarios.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Estructura estándar de respuesta de error
     */
    private Map<String, Object> buildErrorResponse(HttpStatus status, String message, String path) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        error.put("path", path);
        return error;
    }

    /**
     * Errores de validación (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                request.getDescription(false).replace("uri=", "")
        );
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Argumentos ilegales (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(
                buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request.getDescription(false).replace("uri=", "")
                )
        );
    }

    /**
     * Credenciales inválidas (401 Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        
        log.warn("Intento de login fallido: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                buildErrorResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas",
                        request.getDescription(false).replace("uri=", "")
                )
        );
    }

    /**
     * Error de autenticación genérico (401 Unauthorized)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(
            AuthenticationException ex, WebRequest request) {
        
        log.warn("Error de autenticación: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                buildErrorResponse(
                        HttpStatus.UNAUTHORIZED,
                        "No autenticado",
                        request.getDescription(false).replace("uri=", "")
                )
        );
    }

    /**
     * Acceso denegado (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        
        log.warn("Acceso denegado: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                buildErrorResponse(
                        HttpStatus.FORBIDDEN,
                        "No tienes permiso para acceder a este recurso",
                        request.getDescription(false).replace("uri=", "")
                )
        );
    }

    /**
     * Recurso no encontrado (404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.info("Recurso no encontrado: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildErrorResponse(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        request.getDescription(false).replace("uri=", "")
                )
        );
    }

    /**
     * Cualquier otra excepción (500 Internal Server Error)
     * En producción NO expone detalles del error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        
        // Siempre loguear el error completo para debugging
        log.error("Error no manejado: ", ex);
        
        String message;
        if ("prod".equals(activeProfile)) {
            // En producción, no exponer detalles
            message = "Ha ocurrido un error interno. Por favor, intente más tarde.";
        } else {
            // En desarrollo, mostrar el mensaje de error
            message = ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor";
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        message,
                        request.getDescription(false).replace("uri=", "")
                )
        );
    }
}
