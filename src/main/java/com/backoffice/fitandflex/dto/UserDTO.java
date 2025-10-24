package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTOs para la entidad User
 */
public class UserDTO {

    /**
     * DTO para crear un nuevo usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para crear un nuevo usuario",
        example = """
        {
          "name": "Juan Pérez",
          "email": "juan.perez@example.com",
          "password": "password123",
          "phone": "+593-99-1234567",
          "gender": "M",
          "birthDate": "1990-05-15",
          "active": true,
          "roleName": "USER",
          "branchId": 1
        }
        """
    )
    public static class CreateRequest {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nombre completo del usuario",
            example = "Juan Pérez"
        )
        private String name;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 120, message = "El email no puede exceder 120 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Email del usuario (debe ser único)",
            example = "juan.perez@example.com"
        )
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Contraseña del usuario (mínimo 6 caracteres)",
            example = "password123"
        )
        private String password;

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Número de teléfono del usuario",
            example = "+593-99-1234567"
        )
        private String phone;

        @Pattern(regexp = "^[MF]$", message = "El género debe ser M o F")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Género del usuario (M o F)",
            example = "M"
        )
        private String gender;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Fecha de nacimiento del usuario (formato: yyyy-MM-dd)",
            example = "1990-05-15"
        )
        private LocalDate birthDate;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Estado del usuario (activo/inactivo)",
            example = "true"
        )
        private Boolean active;

        @NotBlank(message = "El rol es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nombre del rol del usuario",
            example = "USER",
            allowableValues = {"SUPER_ADMIN", "BRANCH_ADMIN", "USER", "INSTRUCTOR"}
        )
        private String roleName;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la sucursal a la que pertenece el usuario",
            example = "1"
        )
        private Long branchId;
    }

    /**
     * DTO para actualizar un usuario existente
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        private String name;

        @Email(message = "El email debe tener un formato válido")
        @Size(max = 120, message = "El email no puede exceder 120 caracteres")
        private String email;

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        private String phone;

        @Pattern(regexp = "^[MF]$", message = "El género debe ser M o F")
        private String gender;

        private LocalDate birthDate;

        private Boolean active;

        private String roleName;

        private Long branchId;
    }

    /**
     * DTO para respuesta de usuario (información completa)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String gender;
        private LocalDate birthDate;
        private Boolean active;
        private RoleDTO role;
        private BranchDto.Response branch;
        private Instant createdAt;
        private Instant updatedAt;

        public static Response fromEntity(User user) {
            return Response.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .gender(user.getGender())
                    .birthDate(user.getBirthDate())
                    .active(user.getActive())
                    .role(user.getRole() != null ? RoleDTO.fromEntity(user.getRole()) : null)
                    .branch(user.getBranch() != null ? BranchDto.Response.fromEntity(user.getBranch()) : null)
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }

    /**
     * DTO para respuesta simplificada de usuario (solo datos básicos)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryResponse {
        private Long id;
        private String name;
        private String email;
        private Boolean active;
        private String roleName;
        private String branchName;

        public static SummaryResponse fromEntity(User user) {
            return SummaryResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .active(user.getActive())
                    .roleName(user.getRole() != null ? user.getRole().getName() : null)
                    .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                    .build();
        }
    }

    /**
     * DTO para cambio de contraseña
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "La contraseña actual es obligatoria")
        private String currentPassword;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        private String newPassword;
    }
}
