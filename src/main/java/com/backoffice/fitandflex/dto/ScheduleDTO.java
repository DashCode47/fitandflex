package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Schedule;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTOs para la entidad Schedule
 */
public class ScheduleDTO {

    /**
     * DTO para crear un nuevo horario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para crear un nuevo horario",
        example = """
        {
          "startTime": "2024-01-15T09:00:00",
          "endTime": "2024-01-15T10:00:00",
          "active": true,
          "classId": 1
        }
        """
    )
    public static class CreateRequest {
        @NotNull(message = "La hora de inicio es obligatoria")
        @Future(message = "La hora de inicio debe ser en el futuro")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Hora de inicio del horario",
            example = "2024-01-15T09:00:00"
        )
        private LocalDateTime startTime;

        @NotNull(message = "La hora de fin es obligatoria")
        @Future(message = "La hora de fin debe ser en el futuro")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Hora de fin del horario",
            example = "2024-01-15T10:00:00"
        )
        private LocalDateTime endTime;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Estado del horario (activo/inactivo)",
            example = "true"
        )
        private Boolean active;

        @NotNull(message = "El ID de la clase es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID de la clase para la cual se crea el horario",
            example = "1"
        )
        private Long classId;
    }

    /**
     * DTO para actualizar un horario existente
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para actualizar un horario existente",
        example = """
        {
          "startTime": "2024-01-15T09:30:00",
          "endTime": "2024-01-15T10:30:00",
          "active": true
        }
        """
    )
    public static class UpdateRequest {
        @Future(message = "La hora de inicio debe ser en el futuro")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Hora de inicio del horario",
            example = "2024-01-15T09:30:00"
        )
        private LocalDateTime startTime;

        @Future(message = "La hora de fin debe ser en el futuro")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Hora de fin del horario",
            example = "2024-01-15T10:30:00"
        )
        private LocalDateTime endTime;

        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Estado del horario (activo/inactivo)",
            example = "true"
        )
        private Boolean active;
    }

    /**
     * DTO para respuesta de horario (información completa)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información completa de un horario",
        example = """
        {
          "id": 1,
          "startTime": "2024-01-15T09:00:00",
          "endTime": "2024-01-15T10:00:00",
          "active": true,
          "clazz": {
            "id": 1,
            "name": "Yoga Vinyasa",
            "capacity": 20
          },
          "reservationCount": 5,
          "availableSpots": 15,
          "createdAt": "2024-01-15T08:00:00",
          "updatedAt": "2024-01-15T08:00:00"
        }
        """
    )
    public static class Response {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean active;
        private ClassDTO.SummaryResponse clazz;
        private Integer reservationCount;
        private Integer availableSpots;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Schedule schedule) {
            int reservationCount = schedule.getReservations() != null ? schedule.getReservations().size() : 0;
            int capacity = schedule.getClazz() != null ? schedule.getClazz().getCapacity() : 0;
            int availableSpots = Math.max(0, capacity - reservationCount);

            return Response.builder()
                    .id(schedule.getId())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .active(schedule.getActive())
                    .clazz(schedule.getClazz() != null ? ClassDTO.SummaryResponse.fromEntity(schedule.getClazz()) : null)
                    .reservationCount(reservationCount)
                    .availableSpots(availableSpots)
                    .createdAt(schedule.getCreatedAt())
                    .updatedAt(schedule.getUpdatedAt())
                    .build();
        }
    }

    /**
     * DTO para respuesta simplificada de horario (solo datos básicos)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información básica de un horario",
        example = """
        {
          "id": 1,
          "startTime": "2024-01-15T09:00:00",
          "endTime": "2024-01-15T10:00:00",
          "active": true,
          "className": "Yoga Vinyasa",
          "availableSpots": 15
        }
        """
    )
    public static class SummaryResponse {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean active;
        private String className;
        private Integer availableSpots;

        public static SummaryResponse fromEntity(Schedule schedule) {
            int reservationCount = schedule.getReservations() != null ? schedule.getReservations().size() : 0;
            int capacity = schedule.getClazz() != null ? schedule.getClazz().getCapacity() : 0;
            int availableSpots = Math.max(0, capacity - reservationCount);

            return SummaryResponse.builder()
                    .id(schedule.getId())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .active(schedule.getActive())
                    .className(schedule.getClazz() != null ? schedule.getClazz().getName() : null)
                    .availableSpots(availableSpots)
                    .build();
        }
    }
}
