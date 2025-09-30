package com.backoffice.fitandflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product entity: representa una membresía ofrecida en una sucursal
 */
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_branch", columnList = "branch_id"),
                @Index(name = "idx_product_category", columnList = "category"),
                @Index(name = "idx_product_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Información básica del producto
     */
    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 50)
    private String category; // ej: "BASIC", "PREMIUM", "VIP", "FAMILY"

    @Column(length = 20)
    private String sku; // Código único de la membresía

    @Column(length = 20)
    private String membershipType; // ej: "MENSUAL", "SEMESTRAL", "ANUAL"

    /**
     * Precio y duración
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays; // Duración de la membresía en días

    @Column(name = "max_users")
    private Integer maxUsers; // Máximo número de usuarios que pueden usar esta membresía

    /**
     * Estado y configuración
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "auto_renewal")
    @Builder.Default
    private Boolean autoRenewal = false; // Si la membresía se renueva automáticamente

    @Column(name = "trial_period_days")
    private Integer trialPeriodDays; // Período de prueba en días

    /**
     * Información adicional
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "benefits", length = 2000)
    private String benefits; // Beneficios de la membresía

    @Column(name = "features", length = 2000)
    private String features; // Características de la membresía

    /**
     * Relaciones
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_branch"))
    private Branch branch;

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
        if (this.sku == null) {
            this.sku = generateSKU();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Métodos de negocio
     */
    private String generateSKU() {
        // Genera un SKU único basado en el nombre y tipo de membresía
        String prefix = name.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(3, name.length())).toUpperCase();
        String typePrefix = membershipType != null ? membershipType.substring(0, Math.min(3, membershipType.length())) : "MEM";
        return prefix + typePrefix + System.currentTimeMillis() % 1000;
    }

    public boolean isAvailable() {
        return active;
    }

    public boolean hasTrialPeriod() {
        return trialPeriodDays != null && trialPeriodDays > 0;
    }

    public boolean supportsAutoRenewal() {
        return autoRenewal;
    }

    public boolean isUnlimitedUsers() {
        return maxUsers == null || maxUsers <= 0;
    }

    public boolean canAccommodateUsers(Integer userCount) {
        return isUnlimitedUsers() || (maxUsers != null && userCount <= maxUsers);
    }

    public String getMembershipTypeDisplay() {
        if (membershipType == null) {
            return "Personalizada";
        }
        return membershipType;
    }

    public String getDurationDisplay() {
        if (durationDays == null) {
            return "Indefinida";
        }
        
        if (durationDays >= 365) {
            int years = durationDays / 365;
            return years + (years == 1 ? " año" : " años");
        } else if (durationDays >= 30) {
            int months = durationDays / 30;
            return months + (months == 1 ? " mes" : " meses");
        } else {
            return durationDays + (durationDays == 1 ? " día" : " días");
        }
    }
}
