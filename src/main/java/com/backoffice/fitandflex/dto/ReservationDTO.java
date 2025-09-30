package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Reservation;
import com.backoffice.fitandflex.entity.ReservationStatus;
import com.backoffice.fitandflex.entity.Schedule;
import com.backoffice.fitandflex.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTOs para gestión de reservas
 */
public class ReservationDTO {

    /**
     * Request para crear una nueva reserva
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para crear una nueva reserva",
        example = """
        {
          "userId": 1,
          "scheduleId": 1
        }
        """
    )
    public static class CreateRequest {
        @NotNull(message = "El ID del usuario es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID del usuario que hace la reserva",
            example = "1"
        )
        private Long userId;

        @NotNull(message = "El ID del horario es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "ID del horario a reservar",
            example = "1"
        )
        private Long scheduleId;
    }

    /**
     * Request para actualizar una reserva
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Datos para actualizar una reserva",
        example = """
        {
          "status": "CANCELED"
        }
        """
    )
    public static class UpdateRequest {
        @io.swagger.v3.oas.annotations.media.Schema(
            description = "Nuevo estado de la reserva",
            example = "CANCELED",
            allowableValues = {"ACTIVE", "CANCELED", "ATTENDED", "NO_SHOW"}
        )
        private ReservationStatus status;
    }

    /**
     * Response completo de una reserva
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información completa de una reserva",
        example = """
        {
          "id": 1,
          "userId": 1,
          "userName": "Juan Pérez",
          "userEmail": "juan@example.com",
          "scheduleId": 1,
          "className": "Yoga Básico",
          "startTime": "2025-01-15T09:00:00",
          "endTime": "2025-01-15T10:00:00",
          "reservationDate": "2025-01-14T15:30:00",
          "status": "ACTIVE",
          "createdAt": "2025-01-14T15:30:00",
          "updatedAt": "2025-01-14T15:30:00"
        }
        """
    )
    public static class Response {
        private Long id;
        private Long userId;
        private String userName;
        private String userEmail;
        private Long scheduleId;
        private String className;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime reservationDate;
        private ReservationStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * Response resumido de una reserva
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Información resumida de una reserva",
        example = """
        {
          "id": 1,
          "userName": "Juan Pérez",
          "className": "Yoga Básico",
          "startTime": "2025-01-15T09:00:00",
          "status": "ACTIVE"
        }
        """
    )
    public static class SummaryResponse {
        private Long id;
        private String userName;
        private String className;
        private LocalDateTime startTime;
        private ReservationStatus status;
    }

    /**
     * Método para convertir entidad a Response
     */
    public static Response fromEntity(Reservation reservation) {
        return Response.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getName())
                .userEmail(reservation.getUser().getEmail())
                .scheduleId(reservation.getSchedule().getId())
                .className(reservation.getSchedule().getClazz().getName())
                .startTime(reservation.getSchedule().getStartTime())
                .endTime(reservation.getSchedule().getEndTime())
                .reservationDate(reservation.getReservationDate())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    /**
     * Método para convertir entidad a SummaryResponse
     */
    public static SummaryResponse fromEntityToSummary(Reservation reservation) {
        return SummaryResponse.builder()
                .id(reservation.getId())
                .userName(reservation.getUser().getName())
                .className(reservation.getSchedule().getClazz().getName())
                .startTime(reservation.getSchedule().getStartTime())
                .status(reservation.getStatus())
                .build();
    }
}