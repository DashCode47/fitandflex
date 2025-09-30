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
 * DTOs para gestión de productos
 */
public class ProductDTO {

    /**
     * Request para crear un nuevo producto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para crear un nuevo producto",
        example = """
        {
          "name": "Membresia mensual Basica",
          "description": "Membresia mensual Basica",
          "category": "SUPPLEMENT",
          "brand": "FitandFlex",
          "size": "UNIQUE",
          "color": "Blanco",
          "price": 45.99,
          "costPrice": 30.00,
          "stockQuantity": 50,
          "minStockLevel": 10,
          "maxStockLevel": 100,
          "isDigital": false,
          "requiresApproval": false,
          "isSubscription": false,
          "imageUrl": "https://example.com/membresia.jpg",
          "tags": "proteína, whey, deporte",
          "weightGrams": 2000,
          "dimensions": "30x20x5 cm",
          "branchId": 1
        }
        """
    )
    public static class CreateRequest {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nombre del producto",
            example = "Membresia mensual Basica"
        )
        private String name;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Descripción del producto",
            example = "Membresia mensual Basica"
        )
        private String description;

        @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Categoría del producto",
            example = "SUPPLEMENT",
            allowableValues = {"MATERIAL", "CLOTHING", "SUPPLEMENT", "SERVICE", "MEMBERSHIP"}
        )
        private String category;

        @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Código único del producto (SKU)",
            example = "MEM001"
        )
        private String sku;

        @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Marca del producto",
            example = "FitandFlex"
        )
        private String brand;

        @Size(max = 20, message = "El tamaño no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Tamaño del producto",
            example = "MENSUAL",
            allowableValues = {"MENSUAL", "SEMESTRAL", "ANUAL"}
        )
        private String size;

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Precio de venta del producto",
            example = "45.99"
        )
        private BigDecimal price;

        @DecimalMin(value = "0.00", message = "El precio de costo no puede ser negativo")
        @DecimalMax(value = "999999.99", message = "El precio de costo no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El precio de costo debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Precio de costo del producto",
            example = "30.00"
        )
        private BigDecimal costPrice;

        @Min(value = 0, message = "La cantidad en stock no puede ser negativa")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Cantidad en stock del producto",
            example = "50"
        )
        private Integer stockQuantity;

        @Min(value = 0, message = "El nivel mínimo de stock no puede ser negativo")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nivel mínimo de stock para alertas",
            example = "10"
        )
        private Integer minStockLevel;

        @Min(value = 0, message = "El nivel máximo de stock no puede ser negativo")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nivel máximo de stock",
            example = "100"
        )
        private Integer maxStockLevel;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Si el producto está activo",
            example = "true"
        )
        private Boolean active = true;

        @Min(value = 1, message = "La duración de suscripción debe ser al menos 1 día")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Duración de la suscripción en días",
            example = "30"
        )
        private Integer subscriptionDurationDays;

        @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "URL de la imagen del producto",
            example = "https://example.com/protein.jpg"
        )
        private String imageUrl;


        @NotNull(message = "El ID de la sucursal es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la sucursal donde se vende el producto",
            example = "1"
        )
        private Long branchId;
    }

    /**
     * Request para actualizar un producto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para actualizar un producto",
        example = """
        {
          "name": "Membresia mensual Basica",
          "description": "Membresia mensual Basica",
          "price": 49.99,
          "stockQuantity": 75,
          "active": true
        }
        """
    )
    public static class UpdateRequest {
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nombre del producto",
            example = "Membresia mensual Basica"
        )
        private String name;

        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Descripción del producto",
            example = "Membresia mensual Basica"
        )
        private String description;

        @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Categoría del producto",
            example = "MEMBERSHIP"
        )
        private String category;

        @Size(max = 20, message = "El SKU no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Código único del producto (SKU)",
            example = "MEM001"
        )
        private String sku;

        @Size(max = 100, message = "La marca no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Marca del producto",
            example = "FitandFlex"
        )
        private String brand;

        @Size(max = 20, message = "El tamaño no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Tamaño del producto",
            example = "MENSUAL"
        )
        private String size;



        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Precio de venta del producto",
            example = "45.99"
        )
        private BigDecimal price;

        @DecimalMin(value = "0.00", message = "El precio de costo no puede ser negativo")
        @DecimalMax(value = "999999.99", message = "El precio de costo no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El precio de costo debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Precio de costo del producto",
            example = "30.00"
        )
        private BigDecimal costPrice;

        @Min(value = 0, message = "La cantidad en stock no puede ser negativa")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Cantidad en stock del producto",
            example = "50"
        )
        private Integer stockQuantity;

        @Min(value = 0, message = "El nivel mínimo de stock no puede ser negativo")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nivel mínimo de stock para alertas",
            example = "10"
        )
        private Integer minStockLevel;

        @Min(value = 0, message = "El nivel máximo de stock no puede ser negativo")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nivel máximo de stock",
            example = "100"
        )
        private Integer maxStockLevel;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Si el producto está activo",
            example = "true"
        )
        private Boolean active;

        @Min(value = 1, message = "La duración de suscripción debe ser al menos 1 día")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Duración de la suscripción en días",
            example = "30"
        )
        private Integer subscriptionDurationDays;

        @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "URL de la imagen del producto",
            example = "https://example.com/membresia.jpg"
        )
        private String imageUrl;






    }

    /**
     * Request para ajustar stock
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para ajustar el stock de un producto",
        example = """
        {
          "quantity": 10,
          "operation": "ADD",
          "reason": "Restock de proveedor"
        }
        """
    )
    public static class StockAdjustmentRequest {
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Cantidad a ajustar",
            example = "10"
        )
        private Integer quantity;

        @NotNull(message = "La operación es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Tipo de operación",
            example = "ADD",
            allowableValues = {"ADD", "SUBTRACT", "SET"}
        )
        private String operation; // ADD, SUBTRACT, SET

        @Size(max = 500, message = "La razón no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Razón del ajuste de stock",
            example = "Restock de proveedor"
        )
        private String reason;
    }

    /**
     * Response completo de un producto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información completa de un producto",
        example = """
        {
          "id": 1,
          "name": "Membresia mensual Basica",
          "description": "Membresia mensual Basica",
          "category": "SUPPLEMENT",
          "sku": "PROT001",
          "brand": "FitandFlex",
          "size": "MENSUAL",
          "price": 45.99,
          "costPrice": 30.00,
          "stockQuantity": 50,
          "minStockLevel": 10,
          "maxStockLevel": 100,
          "active": true,
          "isDigital": false,
          "requiresApproval": false,
          "isSubscription": false,
          "subscriptionDurationDays": null,
          "imageUrl": "https://example.com/protein.jpg",
          "branchId": 1,
          "branchName": "Sucursal Centro",
          "profitMargin": 0.3478,
          "inStock": true,
          "lowStock": false,
          "overstocked": false,
          "canBeSold": true,
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
        private String brand;
        private String size;
        private BigDecimal price;
        private BigDecimal costPrice;
        private Integer stockQuantity;
        private Integer minStockLevel;
        private Integer maxStockLevel;
        private Boolean active;
        private Boolean isDigital;
        private Boolean requiresApproval;
        private Boolean isSubscription;
        private Integer subscriptionDurationDays;
        private String imageUrl;
        private Long branchId;
        private String branchName;
        private BigDecimal profitMargin;
        private Boolean inStock;
        private Boolean lowStock;
        private Boolean overstocked;
        private Boolean canBeSold;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * Response resumido de un producto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información resumida de un producto",
        example = """
        {
          "id": 1,
          "name": "Membresia mensual Basica",
          "category": "SUPPLEMENT",
          "price": 45.99,
          "stockQuantity": 50,
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
        private BigDecimal price;
        private Integer stockQuantity;
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
                .brand(product.getBrand())
                .size(product.getSize())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .stockQuantity(product.getStockQuantity())
                .minStockLevel(product.getMinStockLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .active(product.getActive())
                .isDigital(product.getIsDigital())
                .requiresApproval(product.getRequiresApproval())
                .isSubscription(product.getIsSubscription())
                .subscriptionDurationDays(product.getSubscriptionDurationDays())
                .imageUrl(product.getImageUrl())
                .branchId(product.getBranch().getId())
                .branchName(product.getBranch().getName())
                .profitMargin(product.getProfitMargin())
                .inStock(product.isInStock())
                .lowStock(product.isLowStock())
                .overstocked(product.isOverstocked())
                .canBeSold(product.canBeSold())
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
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
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .branchName(product.getBranch().getName())
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }
}