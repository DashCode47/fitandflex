package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Payment;
import com.backoffice.fitandflex.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar la relación entre usuario y producto
 */
public class UserProductDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta con información de producto asignado a usuario")
    public static class Response {
        
        @Schema(description = "ID del producto", example = "1")
        private Long productId;
        
        @Schema(description = "Nombre del producto", example = "Membresía Premium")
        private String productName;
        
        @Schema(description = "Descripción del producto", example = "Membresía premium con acceso completo")
        private String productDescription;
        
        @Schema(description = "Categoría del producto", example = "PREMIUM")
        private String category;
        
        @Schema(description = "SKU del producto", example = "MEM001")
        private String sku;
        
        @Schema(description = "Tipo de membresía", example = "MENSUAL")
        private String membershipType;
        
        @Schema(description = "Precio del producto", example = "99.99")
        private BigDecimal price;
        
        @Schema(description = "Duración en días", example = "30")
        private Integer durationDays;
        
        @Schema(description = "Máximo número de usuarios", example = "1")
        private Integer maxUsers;
        
        @Schema(description = "Estado del producto", example = "true")
        private Boolean active;
        
        @Schema(description = "Renovación automática", example = "true")
        private Boolean autoRenewal;
        
        @Schema(description = "Días de período de prueba", example = "7")
        private Integer trialPeriodDays;
        
        @Schema(description = "ID de la sucursal", example = "1")
        private Long branchId;
        
        @Schema(description = "Nombre de la sucursal", example = "Fit & Flex Quito Norte")
        private String branchName;
        
        @Schema(description = "ID del pago", example = "1")
        private Long paymentId;
        
        @Schema(description = "Monto del pago", example = "99.99")
        private BigDecimal paymentAmount;
        
        @Schema(description = "Fecha del pago", example = "2024-01-15T10:00:00")
        private LocalDateTime paymentDate;
        
        @Schema(description = "Estado del pago", example = "COMPLETED")
        private String paymentStatus;
        
        @Schema(description = "Método de pago", example = "CARD")
        private String paymentMethod;
        
        @Schema(description = "ID de la transacción", example = "TXN123456")
        private String transactionId;
        
        @Schema(description = "Fecha de creación del producto", example = "2024-01-01T00:00:00")
        private LocalDateTime productCreatedAt;
        
        @Schema(description = "Fecha de última actualización del producto", example = "2024-01-15T00:00:00")
        private LocalDateTime productUpdatedAt;

        public static Response fromPayment(Payment payment) {
            return Response.builder()
                    .paymentId(payment.getId())
                    .paymentAmount(payment.getAmount())
                    .paymentDate(payment.getPaymentDate())
                    .paymentStatus(payment.getStatus().name())
                    .paymentMethod(payment.getPaymentMethod().name())
                    .transactionId(payment.getTransactionId())
                    .build();
        }
        
        public static Response fromPaymentAndProduct(Payment payment, Product product) {
            return Response.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productDescription(product.getDescription())
                    .category(product.getCategory())
                    .sku(product.getSku())
                    .membershipType(product.getMembershipType())
                    .price(product.getPrice())
                    .durationDays(product.getDurationDays())
                    .maxUsers(product.getMaxUsers())
                    .active(product.getActive())
                    .autoRenewal(product.getAutoRenewal())
                    .trialPeriodDays(product.getTrialPeriodDays())
                    .branchId(product.getBranch().getId())
                    .branchName(product.getBranch().getName())
                    .paymentId(payment.getId())
                    .paymentAmount(payment.getAmount())
                    .paymentDate(payment.getPaymentDate())
                    .paymentStatus(payment.getStatus().name())
                    .paymentMethod(payment.getPaymentMethod().name())
                    .transactionId(payment.getTransactionId())
                    .productCreatedAt(product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                    .productUpdatedAt(product.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request para actualizar información de producto de usuario")
    public static class UpdateProductRequest {
        
        @Schema(description = "Nuevo ID de producto (opcional)", example = "2")
        private Long newProductId;
        
        @Schema(description = "Nuevo monto de pago (opcional)", example = "149.99")
        private BigDecimal newPaymentAmount;
        
        @Schema(description = "Nueva fecha de pago (opcional)", example = "2024-01-20T10:00:00")
        private LocalDateTime newPaymentDate;
        
        @Schema(description = "Nuevo estado de pago (opcional)", example = "COMPLETED")
        private String newPaymentStatus;
        
        @Schema(description = "Nuevo método de pago (opcional)", example = "TRANSFER")
        private String newPaymentMethod;
        
        @Schema(description = "Nueva descripción del pago (opcional)", example = "Pago actualizado")
        private String newPaymentDescription;
    }
}
