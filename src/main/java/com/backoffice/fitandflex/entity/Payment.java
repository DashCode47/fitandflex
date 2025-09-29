package com.backoffice.fitandflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Payment entity: representa un pago realizado por un usuario
 */
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_user", columnList = "user_id"),
                @Index(name = "idx_payment_reservation", columnList = "reservation_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_date", columnList = "payment_date"),
                @Index(name = "idx_payment_transaction_id", columnList = "transaction_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Información del pago
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId; // ID de la transacción del procesador de pagos

    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference; // Referencia del gateway de pagos

    @Column(name = "gateway_response", length = 1000)
    private String gatewayResponse; // Respuesta completa del gateway

    @Column(length = 500)
    private String description;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    /**
     * Relaciones
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", foreignKey = @ForeignKey(name = "fk_payment_reservation"))
    private Reservation reservation;

    /**
     * Timestamps
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Enums
     */
    public enum PaymentStatus {
        PENDING,        // Pago pendiente
        COMPLETED,      // Pago completado exitosamente
        FAILED,         // Pago falló
        CANCELLED,      // Pago cancelado
        REFUNDED,       // Pago reembolsado
        PARTIALLY_REFUNDED // Pago parcialmente reembolsado
    }

    public enum PaymentMethod {
        CASH,           // Efectivo
        CARD,           // Tarjeta de crédito/débito
        TRANSFER,       // Transferencia bancaria
        DIGITAL_WALLET, // Billetera digital (PayPal, etc.)
        CHECK,          // Cheque
        CREDIT          // Crédito interno
    }

    /**
     * Métodos de negocio
     */
    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isRefundable() {
        return status == PaymentStatus.COMPLETED && refundAmount == null;
    }

    public boolean canBeRefunded() {
        return isRefundable() && 
               paymentDate.isAfter(LocalDateTime.now().minusDays(30)); // 30 días para reembolso
    }

    public void markAsCompleted(String transactionId, String gatewayReference) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.gatewayReference = gatewayReference;
    }

    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    public void processRefund(BigDecimal refundAmount, String refundReason) {
        if (!canBeRefunded()) {
            throw new IllegalStateException("Este pago no puede ser reembolsado");
        }
        
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundDate = LocalDateTime.now();
        
        if (refundAmount.compareTo(amount) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public BigDecimal getNetAmount() {
        if (refundAmount != null) {
            return amount.subtract(refundAmount);
        }
        return amount;
    }
}
