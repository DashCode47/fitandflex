package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Class;
import com.backoffice.fitandflex.entity.Reservation;
import com.backoffice.fitandflex.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar la relación entre usuario y clase
 */
public class UserClassDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Respuesta con información de clase asignada a usuario")
    public static class Response {
        
        @Schema(description = "ID de la clase", example = "1")
        private Long classId;
        
        @Schema(description = "Nombre de la clase", example = "Yoga Vinyasa")
        private String className;
        
        @Schema(description = "Descripción de la clase", example = "Clase de yoga dinámico")
        private String classDescription;
        
        @Schema(description = "Capacidad de la clase", example = "20")
        private Integer capacity;
        
        @Schema(description = "Estado de la clase", example = "true")
        private Boolean active;
        
        @Schema(description = "ID de la sucursal", example = "1")
        private Long branchId;
        
        @Schema(description = "Nombre de la sucursal", example = "Fit & Flex Quito Norte")
        private String branchName;
        
        @Schema(description = "ID de la reserva", example = "1")
        private Long reservationId;
        
        @Schema(description = "Fecha de reserva", example = "2024-01-15T10:00:00")
        private LocalDateTime reservationDate;
        
        @Schema(description = "Estado de la reserva", example = "ACTIVE")
        private String reservationStatus;
        
        @Schema(description = "Horarios disponibles para la clase")
        private List<ScheduleInfo> schedules;
        
        @Schema(description = "Fecha de creación de la clase", example = "2024-01-01T00:00:00")
        private LocalDateTime classCreatedAt;
        
        @Schema(description = "Fecha de última actualización de la clase", example = "2024-01-15T00:00:00")
        private LocalDateTime classUpdatedAt;

        public static Response fromReservation(Reservation reservation) {
            Class clazz = reservation.getSchedule().getClazz();
            
            return Response.builder()
                    .classId(clazz.getId())
                    .className(clazz.getName())
                    .classDescription(clazz.getDescription())
                    .capacity(clazz.getCapacity())
                    .active(clazz.getActive())
                    .branchId(clazz.getBranch().getId())
                    .branchName(clazz.getBranch().getName())
                    .reservationId(reservation.getId())
                    .reservationDate(reservation.getReservationDate())
                    .reservationStatus(reservation.getStatus().name())
                    .classCreatedAt(clazz.getCreatedAt())
                    .classUpdatedAt(clazz.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Información de horario")
    public static class ScheduleInfo {
        
        @Schema(description = "ID del horario", example = "1")
        private Long scheduleId;
        
        @Schema(description = "Hora de inicio", example = "2024-01-15T10:00:00")
        private LocalDateTime startTime;
        
        @Schema(description = "Hora de fin", example = "2024-01-15T11:00:00")
        private LocalDateTime endTime;
        
        @Schema(description = "Estado del horario", example = "true")
        private Boolean active;
        
        public static ScheduleInfo fromSchedule(Schedule schedule) {
            return ScheduleInfo.builder()
                    .scheduleId(schedule.getId())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .active(schedule.getActive())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Request para actualizar información de clase de usuario")
    public static class UpdateClassRequest {
        
        @Schema(description = "Nuevo ID de clase (opcional)", example = "2")
        private Long newClassId;
        
        @Schema(description = "Nuevo ID de horario (opcional)", example = "3")
        private Long newScheduleId;
        
        @Schema(description = "Nueva fecha de reserva (opcional)", example = "2024-01-20T10:00:00")
        private LocalDateTime newReservationDate;
        
        @Schema(description = "Nuevo estado de reserva (opcional)", example = "ACTIVE")
        private String newReservationStatus;
    }
}
