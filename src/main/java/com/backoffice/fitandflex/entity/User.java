package com.backoffice.fitandflex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * User entity: representa a un usuario del sistema (Super Admin, Admin Sucursal, Usuario, Instructor, etc.)
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_role", columnList = "role_id"),
                @Index(name = "idx_user_branch", columnList = "branch_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Información básica
     */
    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    @ToString.Include
    private String email;

    @Column(nullable = false)
    private String password; // se almacenará encriptada (BCrypt)

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String gender; // Opcional: "M", "F", "O"

    @Column(name = "birth_date")
    private LocalDate birthDate; // Fecha de nacimiento

    @Column(length = 255)
    private String profileImageUrl;

    /**
     * Estado del usuario
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Relaciones
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role"))
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_user_branch"))
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
