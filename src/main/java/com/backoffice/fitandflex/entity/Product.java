package com.backoffice.fitandflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product entity: representa un producto o servicio adicional ofrecido en una sucursal
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
    private String category; // ej: "MATERIAL", "CLOTHING", "SUPPLEMENT", "SERVICE"

    @Column(length = 20)
    private String sku; // Stock Keeping Unit - código único del producto

    @Column(length = 100)
    private String brand;

    @Column(length = 20)
    private String size; // ej: "S", "M", "L", "XL", "UNIQUE"

    @Column(length = 50)
    private String color;

    /**
     * Precio y disponibilidad
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice; // Precio de costo para calcular ganancias

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "min_stock_level")
    private Integer minStockLevel; // Nivel mínimo de stock para alertas

    @Column(name = "max_stock_level")
    private Integer maxStockLevel; // Nivel máximo de stock

    /**
     * Estado y configuración
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_digital")
    @Builder.Default
    private Boolean isDigital = false; // Si es un producto digital (ej: membresía)

    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = false; // Si requiere aprobación antes de la venta

    @Column(name = "is_subscription")
    @Builder.Default
    private Boolean isSubscription = false; // Si es un producto de suscripción

    @Column(name = "subscription_duration_days")
    private Integer subscriptionDurationDays; // Duración en días si es suscripción

    /**
     * Información adicional
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "tags", length = 500)
    private String tags; // Tags separados por comas para búsqueda

    @Column(name = "weight_grams")
    private Integer weightGrams; // Peso en gramos para envíos

    @Column(name = "dimensions", length = 50)
    private String dimensions; // ej: "30x20x5 cm"

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
        // Genera un SKU único basado en el nombre y timestamp
        String prefix = name.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(3, name.length())).toUpperCase();
        return prefix + System.currentTimeMillis() % 10000;
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isLowStock() {
        return minStockLevel != null && stockQuantity != null && stockQuantity <= minStockLevel;
    }

    public boolean isOverstocked() {
        return maxStockLevel != null && stockQuantity != null && stockQuantity > maxStockLevel;
    }

    public boolean canBeSold() {
        return active && (!requiresApproval || isApproved()) && (isDigital || isInStock());
    }

    private boolean isApproved() {
        // Lógica para verificar si el producto está aprobado
        // Por ahora asumimos que si no requiere aprobación, está aprobado
        return !requiresApproval;
    }

    public void reduceStock(Integer quantity) {
        if (isDigital) {
            return; // Los productos digitales no tienen stock
        }
        
        if (stockQuantity == null || stockQuantity < quantity) {
            throw new IllegalStateException("Stock insuficiente");
        }
        
        this.stockQuantity -= quantity;
    }

    public void increaseStock(Integer quantity) {
        if (isDigital) {
            return; // Los productos digitales no tienen stock
        }
        
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        
        this.stockQuantity += quantity;
    }

    public BigDecimal getProfitMargin() {
        if (costPrice == null || price == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal profit = price.subtract(costPrice);
        return profit.divide(price, 4, java.math.RoundingMode.HALF_UP);
    }

    public boolean isSubscriptionExpired() {
        if (!isSubscription || subscriptionDurationDays == null) {
            return false;
        }
        
        // Lógica para verificar si la suscripción ha expirado
        // Esto se implementaría con una fecha de inicio de suscripción
        return false;
    }
}
