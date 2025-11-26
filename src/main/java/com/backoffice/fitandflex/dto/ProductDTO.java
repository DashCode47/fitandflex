package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Product;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs para gestión de membresías
 */
public class ProductDTO {

    /**
     * Request para crear una nueva membresía
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para crear una nueva membresía",
        example = """
        {
          "name": "Membresía Básica Mensual",
          "description": "Acceso completo a todas las instalaciones y clases grupales",
          "category": "BASIC",
          "membershipType": "MENSUAL",
          "price": 45.99,
          "durationDays": 30,
          "maxUsers": 1,
          "autoRenewal": true,
          "trialPeriodDays": 7,
          "benefits": "Acceso a gimnasio, clases grupales, vestuarios",
          "features": "Sin restricciones de horario, acceso a todas las sucursales",
          "imageUrl": "https://example.com/membresia-basica.jpg",
          "branchId": 1
        }
        """
    )
    public static class CreateRequest {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nombre de la membresía",
            example = "Membresía Básica Mensual"
        )
        private String name;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Descripción de la membresía",
            example = "Acceso completo a todas las instalaciones y clases grupales"
        )
        private String description;

        @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Categoría de la membresía",
            example = "BASIC",
            allowableValues = {"BASIC", "PREMIUM", "VIP", "FAMILY", "STUDENT", "SENIOR"}
        )
        private String category;

        @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Código único de la membresía (SKU)",
            example = "MEMBAS001"
        )
        private String sku;

        @Size(max = 20, message = "El tipo de membresía no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Tipo de membresía",
            example = "MENSUAL",
            allowableValues = {"MENSUAL", "SEMESTRAL", "ANUAL", "PERSONALIZADA"}
        )
        private String membershipType;

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Precio de la membresía",
            example = "45.99"
        )
        private BigDecimal price;

        @NotNull(message = "La duración en días es obligatoria")
        @Min(value = 1, message = "La duración debe ser al menos 1 día")
        @Max(value = 3650, message = "La duración no puede exceder 10 años")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Duración de la membresía en días",
            example = "30"
        )
        private Integer durationDays;

        @Min(value = 1, message = "El máximo de usuarios debe ser al menos 1")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Máximo número de usuarios (null = ilimitado)",
            example = "1"
        )
        private Integer maxUsers;

        @Min(value = 1, message = "El número de clases debe ser al menos 1")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Número de clases incluidas en la membresía (null = ilimitadas)",
            example = "12"
        )
        private Integer numberOfClasses;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Si la membresía está activa",
            example = "true"
        )
        private Boolean active = true;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Si la membresía se renueva automáticamente",
            example = "true"
        )
        private Boolean autoRenewal = false;

        @Min(value = 0, message = "El período de prueba no puede ser negativo")
        @Max(value = 90, message = "El período de prueba no puede exceder 90 días")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Período de prueba en días (0 = sin prueba)",
            example = "7"
        )
        private Integer trialPeriodDays;

        @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "URL de la imagen de la membresía",
            example = "https://example.com/membresia-basica.jpg"
        )
        private String imageUrl;

        @Size(max = 2000, message = "Los beneficios no pueden exceder 2000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Beneficios de la membresía",
            example = "Acceso a gimnasio, clases grupales, vestuarios"
        )
        private String benefits;

        @Size(max = 2000, message = "Las características no pueden exceder 2000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Características de la membresía",
            example = "Sin restricciones de horario, acceso a todas las sucursales"
        )
        private String features;

        @NotNull(message = "El ID de la sucursal es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la sucursal donde se ofrece la membresía",
            example = "1"
        )
        private Long branchId;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la clase asociada al producto (opcional)",
            example = "1"
        )
        private Long classId;
    }

    /**
     * Request para actualizar una membresía
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para actualizar una membresía",
        example = """
        {
          "name": "Membresía Básica Mensual Premium",
          "description": "Acceso completo con beneficios adicionales",
          "price": 49.99,
          "maxUsers": 2,
          "autoRenewal": true
        }
        """
    )
    public static class UpdateRequest {
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nombre de la membresía",
            example = "Membresía Básica Mensual Premium"
        )
        private String name;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Descripción de la membresía",
            example = "Acceso completo con beneficios adicionales"
        )
        private String description;

        @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Categoría de la membresía",
            example = "PREMIUM"
        )
        private String category;

        @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Código único de la membresía (SKU)",
            example = "MEMBAS001"
        )
        private String sku;

        @Size(max = 20, message = "El tipo de membresía no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Tipo de membresía",
            example = "MENSUAL"
        )
        private String membershipType;

        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Precio de la membresía",
            example = "49.99"
        )
        private BigDecimal price;

        @Min(value = 1, message = "La duración debe ser al menos 1 día")
        @Max(value = 3650, message = "La duración no puede exceder 10 años")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Duración de la membresía en días",
            example = "30"
        )
        private Integer durationDays;

        @Min(value = 1, message = "El máximo de usuarios debe ser al menos 1")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Máximo número de usuarios (null = ilimitado)",
            example = "2"
        )
        private Integer maxUsers;

        @Min(value = 1, message = "El número de clases debe ser al menos 1")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Número de clases incluidas en la membresía (null = ilimitadas)",
            example = "12"
        )
        private Integer numberOfClasses;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Si la membresía está activa",
            example = "true"
        )
        private Boolean active;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Si la membresía se renueva automáticamente",
            example = "true"
        )
        private Boolean autoRenewal;

        @Min(value = 0, message = "El período de prueba no puede ser negativo")
        @Max(value = 90, message = "El período de prueba no puede exceder 90 días")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Período de prueba en días (0 = sin prueba)",
            example = "7"
        )
        private Integer trialPeriodDays;

        @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "URL de la imagen de la membresía",
            example = "https://example.com/membresia-premium.jpg"
        )
        private String imageUrl;

        @Size(max = 2000, message = "Los beneficios no pueden exceder 2000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Beneficios de la membresía",
            example = "Acceso a gimnasio, clases grupales, vestuarios, spa"
        )
        private String benefits;

        @Size(max = 2000, message = "Las características no pueden exceder 2000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Características de la membresía",
            example = "Sin restricciones de horario, acceso a todas las sucursales, invitados"
        )
        private String features;
    }

    /**
     * Response completo de una membresía
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información completa de una membresía",
        example = """
        {
          "id": 1,
          "name": "Membresía Básica Mensual",
          "description": "Acceso completo a todas las instalaciones y clases grupales",
          "category": "BASIC",
          "sku": "MEMBAS001",
          "membershipType": "MENSUAL",
          "price": 45.99,
          "durationDays": 30,
          "maxUsers": 1,
          "active": true,
          "autoRenewal": true,
          "trialPeriodDays": 7,
          "imageUrl": "https://example.com/membresia-basica.jpg",
          "benefits": "Acceso a gimnasio, clases grupales, vestuarios",
          "features": "Sin restricciones de horario, acceso a todas las sucursales",
          "branchId": 1,
          "branchName": "Sucursal Centro",
          "isAvailable": true,
          "hasTrialPeriod": true,
          "supportsAutoRenewal": true,
          "isUnlimitedUsers": false,
          "membershipTypeDisplay": "MENSUAL",
          "durationDisplay": "1 mes",
          "createdAt": "2025-01-15T10:30:00",
          "updatedAt": "2025-01-15T10:30:00"
        }
        """
    )
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String category;
        private String sku;
        private String membershipType;
        private BigDecimal price;
        private Integer durationDays;
        private Integer maxUsers;
        private Integer numberOfClasses;
        private Boolean active;
        private Boolean autoRenewal;
        private Integer trialPeriodDays;
        private String imageUrl;
        private String benefits;
        private String features;
        private Long branchId;
        private String branchName;
        private Boolean isAvailable;
        private Boolean hasTrialPeriod;
        private Boolean supportsAutoRenewal;
        private Boolean isUnlimitedUsers;
        private Boolean isUnlimitedClasses;
        private String membershipTypeDisplay;
        private String durationDisplay;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Clase asociada al producto
        private ClassResponse associatedClass;
    }

    /**
     * Response de la clase asociada al producto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "Información de la clase asociada al producto")
    public static class ClassResponse {
        private Long id;
        private String name;
        private String description;
        private Integer capacity;
        private Boolean active;
        private LocalDateTime createdAt;
    }

    /**
     * Response resumido de una membresía
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información resumida de una membresía",
        example = """
        {
          "id": 1,
          "name": "Membresía Básica Mensual",
          "category": "BASIC",
          "membershipType": "MENSUAL",
          "price": 45.99,
          "durationDays": 30,
          "active": true,
          "branchName": "Sucursal Centro",
          "createdAt": "2025-01-15T10:30:00"
        }
        """
    )
    public static class SummaryResponse {
        private Long id;
        private String name;
        private String category;
        private String membershipType;
        private BigDecimal price;
        private Integer durationDays;
        private Boolean active;
        private String branchName;
        private LocalDateTime createdAt;
    }

    /**
     * Método para convertir entidad a Response
     */
    public static Response fromEntity(Product product) {
        return Response.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .sku(product.getSku())
                .membershipType(product.getMembershipType())
                .price(product.getPrice())
                .durationDays(product.getDurationDays())
                .maxUsers(product.getMaxUsers())
                .numberOfClasses(product.getNumberOfClasses())
                .active(product.getActive())
                .autoRenewal(product.getAutoRenewal())
                .trialPeriodDays(product.getTrialPeriodDays())
                .imageUrl(product.getImageUrl())
                .benefits(product.getBenefits())
                .features(product.getFeatures())
                .branchId(product.getBranch().getId())
                .branchName(product.getBranch().getName())
                .isAvailable(product.isAvailable())
                .hasTrialPeriod(product.hasTrialPeriod())
                .supportsAutoRenewal(product.supportsAutoRenewal())
                .isUnlimitedUsers(product.isUnlimitedUsers())
                .isUnlimitedClasses(product.getNumberOfClasses() == null)
                .membershipTypeDisplay(product.getMembershipTypeDisplay())
                .durationDisplay(product.getDurationDisplay())
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .associatedClass(product.getAssociatedClass() != null ? ClassResponse.builder()
                        .id(product.getAssociatedClass().getId())
                        .name(product.getAssociatedClass().getName())
                        .description(product.getAssociatedClass().getDescription())
                        .capacity(product.getAssociatedClass().getCapacity())
                        .active(product.getAssociatedClass().getActive())
                        .createdAt(product.getAssociatedClass().getCreatedAt())
                        .build() : null)
                .build();
    }

    /**
     * Método para convertir entidad a SummaryResponse
     */
    public static SummaryResponse fromEntityToSummary(Product product) {
        return SummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .membershipType(product.getMembershipType())
                .price(product.getPrice())
                .durationDays(product.getDurationDays())
                .active(product.getActive())
                .branchName(product.getBranch().getName())
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }
}