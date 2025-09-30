package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs para gestión de pagos
 */
public class PaymentDTO {

    /**
     * Request para crear un nuevo pago
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para crear un nuevo pago",
        example = """
        {
          "amount": 50.00,
          "currency": "USD",
          "paymentMethod": "CASH",
          "description": "Pago por clase de yoga",
          "userId": 1,
          "reservationId": 1
        }
        """
    )
    public static class CreateRequest {
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @DecimalMax(value = "999999.99", message = "El monto no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El monto debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Monto del pago",
            example = "50.00"
        )
        private BigDecimal amount;

        @NotBlank(message = "La moneda es obligatoria")
        @Size(min = 3, max = 3, message = "La moneda debe tener exactamente 3 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Moneda del pago (ISO 4217)",
            example = "USD"
        )
        @Builder.Default
        private String currency = "USD";

        @NotNull(message = "El método de pago es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Método de pago utilizado",
            example = "CASH",
            allowableValues = {"CASH", "CARD", "TRANSFER", "DIGITAL_WALLET", "CHECK", "CREDIT"}
        )
        private Payment.PaymentMethod paymentMethod;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Descripción del pago",
            example = "Pago por clase de yoga"
        )
        private String description;

        @NotNull(message = "El ID del usuario es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID del usuario que realiza el pago",
            example = "1"
        )
        private Long userId;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la reserva asociada (opcional)",
            example = "1"
        )
        private Long reservationId;

        @Size(max = 100, message = "El ID de transacción no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la transacción externa (opcional)",
            example = "TXN123456789"
        )
        private String transactionId;

        @Size(max = 100, message = "La referencia del gateway no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Referencia del gateway de pagos (opcional)",
            example = "GW_REF_123456"
        )
        private String gatewayReference;
    }

    /**
     * Request para actualizar un pago
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para actualizar un pago",
        example = """
        {
          "status": "COMPLETED",
          "description": "Pago completado exitosamente",
          "transactionId": "TXN123456789",
          "gatewayReference": "GW_REF_123456"
        }
        """
    )
    public static class UpdateRequest {
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nuevo estado del pago",
            example = "COMPLETED",
            allowableValues = {"PENDING", "COMPLETED", "FAILED", "CANCELLED", "REFUNDED", "PARTIALLY_REFUNDED"}
        )
        private Payment.PaymentStatus status;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Descripción actualizada del pago",
            example = "Pago completado exitosamente"
        )
        private String description;

        @Size(max = 100, message = "El ID de transacción no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la transacción externa",
            example = "TXN123456789"
        )
        private String transactionId;

        @Size(max = 100, message = "La referencia del gateway no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Referencia del gateway de pagos",
            example = "GW_REF_123456"
        )
        private String gatewayReference;

        @Size(max = 1000, message = "La respuesta del gateway no puede exceder 1000 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Respuesta completa del gateway",
            example = "{\"status\": \"success\", \"code\": \"200\"}"
        )
        private String gatewayResponse;

        @Size(max = 500, message = "La razón de falla no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Razón de falla del pago",
            example = "Tarjeta rechazada"
        )
        private String failureReason;
    }

    /**
     * Request para procesar un reembolso
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para procesar un reembolso",
        example = """
        {
          "refundAmount": 25.00,
          "refundReason": "Cancelación de clase"
        }
        """
    )
    public static class RefundRequest {
        @NotNull(message = "El monto del reembolso es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto del reembolso debe ser mayor a 0")
        @DecimalMax(value = "999999.99", message = "El monto del reembolso no puede exceder 999,999.99")
        @Digits(integer = 6, fraction = 2, message = "El monto del reembolso debe tener máximo 6 dígitos enteros y 2 decimales")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Monto del reembolso",
            example = "25.00"
        )
        private BigDecimal refundAmount;

        @NotBlank(message = "La razón del reembolso es obligatoria")
        @Size(max = 500, message = "La razón del reembolso no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Razón del reembolso",
            example = "Cancelación de clase"
        )
        private String refundReason;
    }

    /**
     * Response completo de un pago
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información completa de un pago",
        example = """
        {
          "id": 1,
          "amount": 50.00,
          "currency": "USD",
          "paymentDate": "2025-01-15T10:30:00",
          "status": "COMPLETED",
          "paymentMethod": "CASH",
          "transactionId": "TXN123456789",
          "gatewayReference": "GW_REF_123456",
          "description": "Pago por clase de yoga",
          "refundAmount": null,
          "refundDate": null,
          "refundReason": null,
          "netAmount": 50.00,
          "userId": 1,
          "userName": "Juan Pérez",
          "userEmail": "juan@example.com",
          "reservationId": 1,
          "createdAt": "2025-01-15T10:30:00",
          "updatedAt": "2025-01-15T10:30:00"
        }
        """
    )
    public static class Response {
        private Long id;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime paymentDate;
        private Payment.PaymentStatus status;
        private Payment.PaymentMethod paymentMethod;
        private String transactionId;
        private String gatewayReference;
        private String gatewayResponse;
        private String description;
        private String failureReason;
        private BigDecimal refundAmount;
        private LocalDateTime refundDate;
        private String refundReason;
        private BigDecimal netAmount;
        private Long userId;
        private String userName;
        private String userEmail;
        private Long reservationId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * Response resumido de un pago
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información resumida de un pago",
        example = """
        {
          "id": 1,
          "amount": 50.00,
          "currency": "USD",
          "status": "COMPLETED",
          "paymentMethod": "CASH",
          "userName": "Juan Pérez",
          "createdAt": "2025-01-15T10:30:00"
        }
        """
    )
    public static class SummaryResponse {
        private Long id;
        private BigDecimal amount;
        private String currency;
        private Payment.PaymentStatus status;
        private Payment.PaymentMethod paymentMethod;
        private String userName;
        private LocalDateTime createdAt;
    }

    /**
     * Método para convertir entidad a Response
     */
    public static Response fromEntity(Payment payment) {
        return Response.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .gatewayReference(payment.getGatewayReference())
                .gatewayResponse(payment.getGatewayResponse())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .refundAmount(payment.getRefundAmount())
                .refundDate(payment.getRefundDate())
                .refundReason(payment.getRefundReason())
                .netAmount(payment.getNetAmount())
                .userId(payment.getUser().getId())
                .userName(payment.getUser().getName())
                .userEmail(payment.getUser().getEmail())
                .reservationId(payment.getReservation() != null ? payment.getReservation().getId() : null)
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(payment.getUpdatedAt() != null ? payment.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    /**
     * Método para convertir entidad a SummaryResponse
     */
    public static SummaryResponse fromEntityToSummary(Payment payment) {
        return SummaryResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .userName(payment.getUser().getName())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }
}