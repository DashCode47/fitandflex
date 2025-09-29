package com.backoffice.fitandflex.entity;

import com.backoffice.fitandflex.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Role entity: representa roles del sistema (SUPER_ADMIN, BRANCH_ADMIN, USER, INSTRUCTOR, ...)
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_name", columnNames = "name"),
        indexes = @Index(name = "idx_role_name", columnList = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre único del rol (ej: SUPER_ADMIN, BRANCH_ADMIN, USER)
     */
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    /**
     * Timestamps
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Relación con usuarios (opcional, lazy to avoid fetch cost).
     * - mappedBy debe coincidir con el nombre del atributo en User que referencia a Role.
     * - Excluimos del toString/equals para evitar loops y problemas de rendimiento.
     */
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude
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
}
