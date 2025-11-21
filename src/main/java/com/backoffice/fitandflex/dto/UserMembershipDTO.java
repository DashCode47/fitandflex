package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.UserMembership;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para gestión de membresías de usuario
 */
public class UserMembershipDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request para crear membresía de usuario")
    public static class CreateRequest {
        
        @Schema(description = "ID del usuario", example = "1", required = true)
        private Long userId;
        
        @Schema(description = "ID del producto (membresía)", example = "1", required = true)
        private Long productId;
        
        @Schema(description = "Fecha de inicio de la membresía", example = "2024-01-15T00:00:00", required = true)
        private LocalDateTime startDate;
        
        @Schema(description = "Fecha de fin de la membresía", example = "2024-02-15T23:59:59", required = true)
        private LocalDateTime endDate;
        
        @Schema(description = "Monto inicial pagado (abono)", example = "50.00")
        @DecimalMin(value = "0.00", message = "El abono inicial debe ser mayor o igual a cero")
        @Digits(integer = 6, fraction = 2, message = "El abono debe tener máximo 6 dígitos enteros y 2 decimales")
        private BigDecimal initialPayment;
        
        @Schema(description = "Notas adicionales", example = "Membresía premium asignada por promoción")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request para actualizar membresía de usuario")
    public static class UpdateRequest {
        
        @Schema(description = "Nueva fecha de inicio", example = "2024-01-20T00:00:00")
        private LocalDateTime startDate;
        
        @Schema(description = "Nueva fecha de fin", example = "2024-02-20T23:59:59")
        private LocalDateTime endDate;
        
        @Schema(description = "Nuevo estado", example = "ACTIVE", allowableValues = {"ACTIVE", "EXPIRED", "CANCELLED", "SUSPENDED"})
        private String status;
        
        @Schema(description = "Si está activa", example = "true")
        private Boolean active;
        
        @Schema(description = "Notas adicionales", example = "Membresía extendida por 30 días")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta con información de membresía de usuario")
    public static class Response {
        
        @Schema(description = "ID de la membresía", example = "1")
        private Long id;
        
        @Schema(description = "ID del usuario", example = "1")
        private Long userId;
        
        @Schema(description = "Nombre del usuario", example = "Juan Pérez")
        private String userName;
        
        @Schema(description = "Email del usuario", example = "juan@example.com")
        private String userEmail;
        
        @Schema(description = "ID del producto", example = "1")
        private Long productId;
        
        @Schema(description = "Nombre del producto", example = "Membresía Premium")
        private String productName;
        
        @Schema(description = "Descripción del producto", example = "Membresía premium con acceso completo")
        private String productDescription;
        
        @Schema(description = "Categoría del producto", example = "PREMIUM")
        private String productCategory;
        
        @Schema(description = "Tipo de membresía", example = "MENSUAL")
        private String membershipType;
        
        @Schema(description = "Precio del producto", example = "99.99")
        private java.math.BigDecimal productPrice;
        
        @Schema(description = "Fecha de inicio", example = "2024-01-15T00:00:00")
        private LocalDateTime startDate;
        
        @Schema(description = "Fecha de fin", example = "2024-02-15T23:59:59")
        private LocalDateTime endDate;
        
        @Schema(description = "Estado de la membresía", example = "ACTIVE")
        private String status;
        
        @Schema(description = "Si está activa", example = "true")
        private Boolean active;
        
        @Schema(description = "Notas", example = "Membresía asignada por promoción")
        private String notes;
        
        @Schema(description = "Días restantes", example = "15")
        private Long daysRemaining;
        
        @Schema(description = "Si está vencida", example = "false")
        private Boolean expired;
        
        @Schema(description = "ID de la sucursal", example = "1")
        private Long branchId;
        
        @Schema(description = "Nombre de la sucursal", example = "Fit & Flex Quito Norte")
        private String branchName;
        
        @Schema(description = "Quién asignó la membresía", example = "admin@fitandflex.com")
        private String assignedBy;
        
        @Schema(description = "Fecha de creación", example = "2024-01-15T10:00:00")
        private LocalDateTime createdAt;
        
        @Schema(description = "Fecha de última actualización", example = "2024-01-20T15:30:00")
        private LocalDateTime updatedAt;
        
        @Schema(description = "Monto total de la membresía", example = "99.99")
        private BigDecimal totalAmount;
        
        @Schema(description = "Monto pagado hasta ahora", example = "50.00")
        private BigDecimal paidAmount;
        
        @Schema(description = "Saldo pendiente", example = "49.99")
        private BigDecimal pendingAmount;
        
        @Schema(description = "Si está completamente pagada", example = "false")
        private Boolean fullyPaid;

        public static Response fromEntity(UserMembership membership) {
            return Response.builder()
                    .id(membership.getId())
                    .userId(membership.getUser().getId())
                    .userName(membership.getUser().getName())
                    .userEmail(membership.getUser().getEmail())
                    .productId(membership.getProduct().getId())
                    .productName(membership.getProduct().getName())
                    .productDescription(membership.getProduct().getDescription())
                    .productCategory(membership.getProduct().getCategory())
                    .membershipType(membership.getProduct().getMembershipType())
                    .productPrice(membership.getProduct().getPrice())
                    .startDate(membership.getStartDate())
                    .endDate(membership.getEndDate())
                    .status(membership.getStatus())
                    .active(membership.getActive())
                    .notes(membership.getNotes())
                    .daysRemaining(membership.getDaysRemaining())
                    .expired(membership.isExpired())
                    .branchId(membership.getProduct().getBranch().getId())
                    .branchName(membership.getProduct().getBranch().getName())
                    .assignedBy(membership.getAssignedBy() != null ? membership.getAssignedBy().getEmail() : null)
                    .createdAt(membership.getCreatedAt())
                    .updatedAt(membership.getUpdatedAt())
                    .totalAmount(membership.getTotalAmount())
                    .paidAmount(membership.getPaidAmount())
                    .pendingAmount(membership.getPendingAmount())
                    .fullyPaid(membership.isFullyPaid())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen de membresía de usuario")
    public static class SummaryResponse {
        
        @Schema(description = "ID de la membresía", example = "1")
        private Long id;
        
        @Schema(description = "Nombre del producto", example = "Membresía Premium")
        private String productName;
        
        @Schema(description = "Fecha de inicio", example = "2024-01-15T00:00:00")
        private LocalDateTime startDate;
        
        @Schema(description = "Fecha de fin", example = "2024-02-15T23:59:59")
        private LocalDateTime endDate;
        
        @Schema(description = "Estado", example = "ACTIVE")
        private String status;
        
        @Schema(description = "Días restantes", example = "15")
        private Long daysRemaining;

        public static SummaryResponse fromEntity(UserMembership membership) {
            return SummaryResponse.builder()
                    .id(membership.getId())
                    .productName(membership.getProduct().getName())
                    .startDate(membership.getStartDate())
                    .endDate(membership.getEndDate())
                    .status(membership.getStatus())
                    .daysRemaining(membership.getDaysRemaining())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request para cambiar estado de membresía")
    public static class ChangeStatusRequest {
        
        @Schema(description = "Nuevo estado", example = "CANCELLED", required = true, 
                allowableValues = {"ACTIVE", "EXPIRED", "CANCELLED", "SUSPENDED"})
        private String status;
        
        @Schema(description = "Razón del cambio", example = "Cancelación solicitada por el usuario")
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request para extender membresía")
    public static class ExtendRequest {
        
        @Schema(description = "Días adicionales", example = "30", required = true)
        private Integer additionalDays;
        
        @Schema(description = "Razón de la extensión", example = "Compensación por inconvenientes")
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request para registrar un abono adicional a una membresía")
    public static class AddPaymentRequest {
        
        @Schema(description = "Monto del abono", example = "25.00", required = true)
        @DecimalMin(value = "0.01", message = "El monto del abono debe ser mayor a cero")
        @Digits(integer = 6, fraction = 2, message = "El monto debe tener máximo 6 dígitos enteros y 2 decimales")
        private BigDecimal amount;
        
        @Schema(description = "Método de pago", example = "CASH", required = true)
        private com.backoffice.fitandflex.entity.Payment.PaymentMethod paymentMethod;
        
        @Schema(description = "Descripción del pago", example = "Abono parcial de membresía")
        private String description;
        
        @Schema(description = "ID de transacción (opcional)", example = "TXN123456")
        private String transactionId;
    }
}
