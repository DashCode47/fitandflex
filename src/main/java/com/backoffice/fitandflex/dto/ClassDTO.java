package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Class;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs para la entidad Class
 */
public class ClassDTO {

    /**
     * DTO para crear una nueva clase
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Datos para crear una nueva clase", example = """
            {
              "name": "Yoga Vinyasa",
              "description": "Clase de yoga dinámico que combina respiración y movimiento",
              "capacity": 20,
              "active": true,
              "branchId": 1
            }
            """)
    public static class CreateRequest {
        @NotBlank(message = "El nombre de la clase es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Nombre de la clase", example = "Yoga Vinyasa")
        private String name;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Descripción detallada de la clase", example = "Clase de yoga dinámico que combina respiración y movimiento")
        private String description;

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Capacidad máxima de la clase", example = "20")
        private Integer capacity;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Estado de la clase (activa/inactiva)", example = "true")
        private Boolean active;

        @NotNull(message = "El ID de la sucursal es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID de la sucursal donde se imparte la clase", example = "1")
        private Long branchId;
    }

    /**
     * DTO para actualizar una clase existente
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Datos para actualizar una clase existente", example = """
                {
              "name": "Yoga Vinyasa",
              "description": "Clase de yoga dinámico que combina respiración y movimiento",
              "capacity": 20,
              "active": true,
              "branchId": 1
            }
                    """)
    public static class UpdateRequest {
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Nombre de la clase", example = "Yoga Vinyasa Avanzado")
        private String name;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Descripción detallada de la clase", example = "Clase de yoga dinámico para practicantes avanzados")
        private String description;

        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Capacidad máxima de la clase", example = "15")
        private Integer capacity;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Estado de la clase (activa/inactiva)", example = "true")
        private Boolean active;
    }

    /**
     * DTO para respuesta de clase (información completa)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Información completa de una clase", example = """
            {
              "id": 1,
              "name": "Yoga Vinyasa",
              "description": "Clase de yoga dinámico que combina respiración y movimiento",
              "capacity": 20,
              "active": true,
              "branch": {
                "id": 1,
                "name": "Fit & Flex Quito Norte"
              },
              "createdBy": {
                "id": 1,
                "name": "Admin Sucursal"
              },
              "createdAt": "2024-01-15T10:30:00",
              "updatedAt": "2024-01-15T10:30:00"
            }
            """)
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Integer capacity;
        private Boolean active;
        private BranchDto.Response branch;
        private UserDTO.SummaryResponse createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Class clazz) {
            return Response.builder()
                    .id(clazz.getId())
                    .name(clazz.getName())
                    .description(clazz.getDescription())
                    .capacity(clazz.getCapacity())
                    .active(clazz.getActive())
                    .branch(clazz.getBranch() != null ? BranchDto.Response.fromEntity(clazz.getBranch()) : null)
                    .createdBy(clazz.getCreatedBy() != null ? UserDTO.SummaryResponse.fromEntity(clazz.getCreatedBy())
                            : null)
                    .createdAt(clazz.getCreatedAt())
                    .updatedAt(clazz.getUpdatedAt())
                    .build();
        }
    }

    /**
     * DTO para respuesta simplificada de clase (solo datos básicos)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Información básica de una clase", example = """
            {
              "id": 1,
              "name": "Yoga Vinyasa",
              "capacity": 20,
              "active": true,
              "branchName": "Fit & Flex Quito Norte"
            }
            """)
    public static class SummaryResponse {
        private Long id;
        private String name;
        private Integer capacity;
        private Boolean active;
        private String branchName;

        public static SummaryResponse fromEntity(Class clazz) {
            return SummaryResponse.builder()
                    .id(clazz.getId())
                    .name(clazz.getName())
                    .capacity(clazz.getCapacity())
                    .active(clazz.getActive())
                    .branchName(clazz.getBranch() != null ? clazz.getBranch().getName() : null)
                    .build();
        }
    }
}
