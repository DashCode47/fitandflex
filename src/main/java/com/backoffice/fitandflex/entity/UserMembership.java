package com.backoffice.fitandflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * UserMembership entity: representa la membresía asignada a un usuario
 */
@Entity
@Table(
        name = "user_memberships",
        indexes = {
                @Index(name = "idx_user_membership_user", columnList = "user_id"),
                @Index(name = "idx_user_membership_product", columnList = "product_id"),
                @Index(name = "idx_user_membership_assigned_by", columnList = "assigned_by"),
                @Index(name = "idx_user_membership_status", columnList = "status"),
                @Index(name = "idx_user_membership_active", columnList = "active"),
                @Index(name = "idx_user_membership_dates", columnList = "start_date, end_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UserMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Usuario al que se le asigna la membresía
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_membership_user"))
    private User user;

    /**
     * Producto (membresía) asignado
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_membership_product"))
    private Product product;

    /**
     * Fecha de inicio de la membresía
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Fecha de fin de la membresía
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Estado de la membresía
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, CANCELLED, SUSPENDED

    /**
     * Si la membresía está activa
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Notas adicionales sobre la membresía
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Información de pago
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Quién asignó la membresía
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", foreignKey = @ForeignKey(name = "fk_user_membership_assigned_by"))
    private User assignedBy;

    /**
     * Timestamps
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Métodos de negocio
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.endDate);
    }

    public boolean isActive() {
        return this.active && "ACTIVE".equals(this.status) && !isExpired();
    }

    public long getDaysRemaining() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.endDate).toDays();
    }

    public void expire() {
        this.status = "EXPIRED";
        this.active = false;
    }

    public void cancel() {
        this.status = "CANCELLED";
        this.active = false;
    }

    public void suspend() {
        this.status = "SUSPENDED";
        this.active = false;
    }

    public void activate() {
        this.status = "ACTIVE";
        this.active = true;
    }

    /**
     * Métodos relacionados con pagos
     */
    public BigDecimal getPendingAmount() {
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal pending = totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        return pending.compareTo(BigDecimal.ZERO) > 0 ? pending : BigDecimal.ZERO;
    }

    public boolean isFullyPaid() {
        return getPendingAmount().compareTo(BigDecimal.ZERO) == 0;
    }

    public void addPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
        BigDecimal newPaidAmount = this.paidAmount.add(amount);
        if (newPaidAmount.compareTo(this.totalAmount) > 0) {
            throw new IllegalArgumentException("El monto pagado no puede exceder el monto total de la membresía");
        }
        this.paidAmount = newPaidAmount;
    }

    /**
     * Estados de membresía
     */
    public enum MembershipStatus {
        ACTIVE("Activa"),
        EXPIRED("Vencida"),
        CANCELLED("Cancelada"),
        SUSPENDED("Suspendida");

        private final String displayName;

        MembershipStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
