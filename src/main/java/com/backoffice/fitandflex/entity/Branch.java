package com.backoffice.fitandflex.entity;
import com.backoffice.fitandflex.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


/**
 * Branch entity: representa una sucursal de Fit & Flex
 */
@Entity
@Table(
        name = "branches",
        uniqueConstraints = @UniqueConstraint(name = "uk_branch_name", columnNames = "name"),
        indexes = {
                @Index(name = "idx_branch_name", columnList = "name"),
                @Index(name = "idx_branch_city", columnList = "city")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la sucursal (ej: "Fit & Flex Quito Norte")
     */
    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    /**
     * Información de ubicación
     */
    @Column(length = 150)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 50)
    private String country;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    /**
     * Timestamps
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Relación con usuarios de la sucursal
     */
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters manuales para resolver problema de Lombok
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
