package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Branch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

/**
 * DTOs para la entidad Branch
 */
public class BranchDto {

    /**
     * DTO para crear una nueva sucursal
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para crear una nueva sucursal",
        example = """
        {
          "name": "Fit & Flex Quito Norte",
          "address": "Av. Amazonas N12-123",
          "city": "Quito",
          "state": "Pichincha",
          "country": "Ecuador",
          "phone": "+593-2-1234567",
          "email": "quito.norte@fitandflex.com"
        }
        """
    )
    public static class CreateRequest {
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nombre de la sucursal",
            example = "Fit & Flex Quito Norte"
        )
        private String name;

        @Size(max = 150, message = "La dirección no puede exceder 150 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Dirección de la sucursal",
            example = "Av. Amazonas N12-123"
        )
        private String address;

        @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Ciudad donde se encuentra la sucursal",
            example = "Quito"
        )
        private String city;

        @Size(max = 100, message = "El estado no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Estado o provincia",
            example = "Pichincha"
        )
        private String state;

        @Size(max = 50, message = "El país no puede exceder 50 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "País donde se encuentra la sucursal",
            example = "Ecuador"
        )
        private String country;

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Número de teléfono de la sucursal",
            example = "+593-2-1234567"
        )
        private String phone;

        @Email(message = "El email debe tener un formato válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Email de contacto de la sucursal",
            example = "quito.norte@fitandflex.com"
        )
        private String email;
    }

    /**
     * DTO para actualizar una sucursal existente
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        private String name;

        @Size(max = 150, message = "La dirección no puede exceder 150 caracteres")
        private String address;

        @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
        private String city;

        @Size(max = 100, message = "El estado no puede exceder 100 caracteres")
        private String state;

        @Size(max = 50, message = "El país no puede exceder 50 caracteres")
        private String country;

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        private String phone;

        @Email(message = "El email debe tener un formato válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        private String email;
    }

    /**
     * DTO para respuesta de sucursal (información completa)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String address;
        private String city;
        private String state;
        private String country;
        private String phone;
        private String email;
        private Instant createdAt;
        private Instant updatedAt;

        public static Response fromEntity(Branch branch) {
            return Response.builder()
                    .id(branch.getId())
                    .name(branch.getName())
                    .address(branch.getAddress())
                    .city(branch.getCity())
                    .state(branch.getState())
                    .country(branch.getCountry())
                    .phone(branch.getPhone())
                    .email(branch.getEmail())
                    .createdAt(branch.getCreatedAt())
                    .updatedAt(branch.getUpdatedAt())
                    .build();
        }
    }

    /**
     * DTO para respuesta simplificada de sucursal (solo datos básicos)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryResponse {
        private Long id;
        private String name;
        private String city;
        private String phone;

        public static SummaryResponse fromEntity(Branch branch) {
            return SummaryResponse.builder()
                    .id(branch.getId())
                    .name(branch.getName())
                    .city(branch.getCity())
                    .phone(branch.getPhone())
                    .build();
        }
    }
}