package com.backoffice.fitandflex.dto;

import com.backoffice.fitandflex.entity.Class;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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

        @io.swagger.v3.oas.annotations.media.Schema(description = "Lista de horarios por día de la semana. Cada día puede tener múltiples rangos de horas.", example = """
                [
                  {
                    "dayOfWeek": 1,
                    "timeRanges": [
                      {
                        "startTime": "10:00:00",
                        "endTime": "11:30:00"
                      },
                      {
                        "startTime": "18:00:00",
                        "endTime": "19:30:00"
                      }
                    ]
                  },
                  {
                    "dayOfWeek": 3,
                    "timeRanges": [
                      {
                        "startTime": "10:00:00",
                        "endTime": "11:30:00"
                      }
                    ]
                  }
                ]
                """)
        @Valid
        private List<DaySchedule> schedules;
    }

    /**
     * DTO para representar un día de la semana con sus rangos de horas
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Horarios de un día de la semana (1=Lunes, 7=Domingo)")
    public static class DaySchedule {
        @NotNull(message = "El día de la semana es obligatorio")
        @Min(value = 1, message = "El día de la semana debe estar entre 1 (Lunes) y 7 (Domingo)")
        @Max(value = 7, message = "El día de la semana debe estar entre 1 (Lunes) y 7 (Domingo)")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Día de la semana (1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado, 7=Domingo)", example = "1")
        private Integer dayOfWeek;

        @NotNull(message = "Los rangos de horas son obligatorios")
        @NotEmpty(message = "Debe haber al menos un rango de horas")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Lista de rangos de horas para este día", example = """
                [
                  {
                    "startTime": "10:00:00",
                    "endTime": "11:30:00"
                  }
                ]
                """)
        @Valid
        private List<TimeRange> timeRanges;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Indica si el horario es recurrente (se repite cada semana)", example = "true")
        private Boolean recurrent;
    }

    /**
     * DTO para asignar día de la semana desde una fecha del calendario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Datos para asignar un día desde una fecha del calendario", example = """
            {
              "date": "2024-01-15",
              "startTime": "10:00:00",
              "endTime": "11:30:00",
              "recurrent": true
            }
            """)
    public static class AssignDayFromDateRequest {
        @NotNull(message = "La fecha es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Fecha seleccionada del calendario (formato yyyy-MM-dd)", example = "2024-01-15")
        private java.time.LocalDate date;

        @NotNull(message = "La hora de inicio es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Hora de inicio (formato HH:mm:ss)", example = "10:00:00")
        private LocalTime startTime;

        @NotNull(message = "La hora de fin es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Hora de fin (formato HH:mm:ss)", example = "11:30:00")
        private LocalTime endTime;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Indica si el horario es recurrente (se repite cada semana en el mismo día)", example = "true")
        @Builder.Default
        private Boolean recurrent = false;
    }

    /**
     * DTO para representar un rango de horas
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Rango de horas (hora inicio y hora fin)")
    public static class TimeRange {
        @NotNull(message = "La hora de inicio es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Hora de inicio del rango (formato HH:mm:ss)", example = "10:00:00")
        private LocalTime startTime;

        @NotNull(message = "La hora de fin es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Hora de fin del rango (formato HH:mm:ss)", example = "11:30:00")
        private LocalTime endTime;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Número de usuarios suscritos activamente a este horario específico", example = "5")
        private Integer subscriptionCount;
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

        @io.swagger.v3.oas.annotations.media.Schema(description = "Lista de horarios por día de la semana. Cada día puede tener múltiples rangos de horas. Si se proporciona, se actualizarán los horarios de la clase.", example = """
                [
                  {
                    "dayOfWeek": 1,
                    "timeRanges": [
                      {
                        "startTime": "10:00:00",
                        "endTime": "11:30:00"
                      }
                    ]
                  }
                ]
                """)
        @Valid
        private List<DaySchedule> schedules;
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
              "schedules": [
                {
                  "dayOfWeek": 1,
                  "timeRanges": [
                    {
                      "startTime": "10:00:00",
                      "endTime": "11:30:00"
                    },
                    {
                      "startTime": "18:00:00",
                      "endTime": "19:30:00"
                    }
                  ]
                },
                {
                  "dayOfWeek": 3,
                  "timeRanges": [
                    {
                      "startTime": "10:00:00",
                      "endTime": "11:30:00"
                    }
                  ]
                }
              ],
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
        @io.swagger.v3.oas.annotations.media.Schema(description = "Número de usuarios suscritos activamente a la clase", example = "5")
        private Integer subscriptionCount;
        private BranchDto.Response branch;
        private UserDTO.SummaryResponse createdBy;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Lista de patrones de horarios de la clase (día de la semana y rangos de horas)")
        private List<DaySchedule> schedules;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Class clazz) {
            return fromEntity(clazz, null, null);
        }
        
        public static Response fromEntity(Class clazz, java.util.Collection<com.backoffice.fitandflex.entity.ClassSchedulePattern> patterns) {
            return fromEntity(clazz, patterns, null);
        }
        
        public static Response fromEntity(Class clazz, java.util.Collection<com.backoffice.fitandflex.entity.ClassSchedulePattern> patterns, Integer subscriptionCount) {
            return fromEntity(clazz, patterns, subscriptionCount, null);
        }
        
        public static Response fromEntity(Class clazz, java.util.Collection<com.backoffice.fitandflex.entity.ClassSchedulePattern> patterns, Integer subscriptionCount, 
                                         com.backoffice.fitandflex.repository.ClassSubscriptionRepository subscriptionRepository) {
            // Convertir patrones de horarios a formato DaySchedule
            List<DaySchedule> daySchedules = null;
            
            // Usar los patrones proporcionados o los de la entidad
            java.util.Collection<com.backoffice.fitandflex.entity.ClassSchedulePattern> patternsToUse = 
                patterns != null ? patterns : 
                (clazz.getSchedulePatterns() != null ? clazz.getSchedulePatterns() : java.util.Collections.emptyList());
            
            if (patternsToUse != null && !patternsToUse.isEmpty()) {
                // Agrupar patrones por día de la semana
                java.util.Map<Integer, List<com.backoffice.fitandflex.entity.ClassSchedulePattern>> patternsByDay = 
                    patternsToUse.stream()
                        .filter(pattern -> pattern.getActive())
                        .collect(Collectors.groupingBy(
                            com.backoffice.fitandflex.entity.ClassSchedulePattern::getDayOfWeek
                        ));
                
                daySchedules = patternsByDay.entrySet().stream()
                    .map(entry -> {
                        List<TimeRange> timeRanges = entry.getValue().stream()
                            .map(pattern -> {
                                TimeRange.TimeRangeBuilder builder = TimeRange.builder()
                                    .startTime(pattern.getStartTime())
                                    .endTime(pattern.getEndTime());
                                
                                // Calcular conteo de suscripciones para este horario específico y día de la semana
                                if (subscriptionRepository != null && clazz.getId() != null) {
                                    Long count = subscriptionRepository.countActiveSubscriptionsByClassDayAndTimeRange(
                                        clazz.getId(),
                                        entry.getKey(), // dayOfWeek del patrón
                                        pattern.getStartTime(),
                                        pattern.getEndTime()
                                    );
                                    builder.subscriptionCount(count != null ? count.intValue() : 0);
                                } else {
                                    builder.subscriptionCount(0);
                                }
                                
                                return builder.build();
                            })
                            .collect(Collectors.toList());
                        
                        // Obtener el valor de recurrent del primer patrón del día (todos deberían tener el mismo valor)
                        Boolean recurrent = entry.getValue().isEmpty() ? false : 
                            entry.getValue().get(0).getRecurrent() != null ? 
                            entry.getValue().get(0).getRecurrent() : false;
                        
                        return DaySchedule.builder()
                            .dayOfWeek(entry.getKey())
                            .timeRanges(timeRanges)
                            .recurrent(recurrent)
                            .build();
                    })
                    .sorted(java.util.Comparator.comparing(DaySchedule::getDayOfWeek))
                    .collect(Collectors.toList());
            }
            
            return Response.builder()
                    .id(clazz.getId())
                    .name(clazz.getName())
                    .description(clazz.getDescription())
                    .capacity(clazz.getCapacity())
                    .active(clazz.getActive())
                    .subscriptionCount(subscriptionCount != null ? subscriptionCount : 0)
                    .branch(clazz.getBranch() != null ? BranchDto.Response.fromEntity(clazz.getBranch()) : null)
                    .createdBy(clazz.getCreatedBy() != null ? UserDTO.SummaryResponse.fromEntity(clazz.getCreatedBy())
                            : null)
                    .schedules(daySchedules)
                    .createdAt(clazz.getCreatedAt())
                    .updatedAt(clazz.getUpdatedAt())
                    .build();
        }
    }

    /**
     * DTO para respuesta de clase con fecha específica
     * Usado cuando se consultan clases para una fecha específica
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Información de clase para una fecha específica", example = """
            {
              "id": 1,
              "name": "Yoga Vinyasa",
              "description": "Clase de yoga dinámico",
              "capacity": 20,
              "active": true,
              "subscriptionCount": 5,
              "date": "2025-11-18",
              "dayOfWeek": 1,
              "timeRanges": [
                {
                  "startTime": "10:00:00",
                  "endTime": "11:30:00",
                  "subscriptionCount": 3
                }
              ]
            }
            """)
    public static class ResponseWithDate {
        private Long id;
        private String name;
        private String description;
        private Integer capacity;
        private Boolean active;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Número de usuarios suscritos activamente a la clase", example = "5")
        private Integer subscriptionCount;
        private BranchDto.Response branch;
        private UserDTO.SummaryResponse createdBy;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Fecha específica para la cual se consultan los horarios", example = "2025-11-18")
        private java.time.LocalDate date;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Día de la semana (1=Lunes, 7=Domingo)", example = "1")
        private Integer dayOfWeek;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Lista de rangos de horas para esta fecha específica")
        private List<TimeRange> timeRanges;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
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

    /**
     * DTO para crear una suscripción/reserva de usuario a clase
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Datos para crear una suscripción de usuario a clase. Cada suscripción es para una fecha específica.", example = """
            {
              "userId": 1,
              "startTime": "09:00:00",
              "endTime": "10:00:00",
              "date": "2025-11-18"
            }
            """)
    public static class CreateSubscriptionRequest {
        @NotNull(message = "El ID del usuario es obligatorio")
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID del usuario que se suscribe", example = "1")
        private Long userId;

        @NotNull(message = "La hora de inicio es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Hora de inicio del rango de horas (formato HH:mm:ss)", example = "09:00:00")
        private LocalTime startTime;

        @NotNull(message = "La hora de fin es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Hora de fin del rango de horas (formato HH:mm:ss)", example = "10:00:00")
        private LocalTime endTime;

        @NotNull(message = "La fecha es obligatoria")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Fecha específica de la suscripción (formato yyyy-MM-dd). Cada suscripción es para una fecha específica. Si deseas suscribirte a múltiples fechas, debes crear múltiples suscripciones.", example = "2025-11-18")
        private java.time.LocalDate date;
    }

    /**
     * DTO para respuesta de suscripción
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(description = "Información de una suscripción de usuario a clase", example = """
            {
              "id": 1,
              "userId": 1,
              "classId": 5,
              "className": "Yoga Vinyasa",
              "startTime": "09:00:00",
              "endTime": "10:00:00",
              "date": "2024-01-15",
              "recurrent": false,
              "active": true,
              "createdAt": "2024-01-15T10:30:00",
              "updatedAt": "2024-01-15T10:30:00"
            }
            """)
    public static class SubscriptionResponse {
        private Long id;
        private Long userId;
        private Long classId;
        private String className;
        private BranchDto.Response branch;
        private LocalTime startTime;
        private LocalTime endTime;
        private java.time.LocalDate date;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Día de la semana (1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado, 7=Domingo)", example = "1")
        private Integer dayOfWeek;
        private Boolean recurrent;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static SubscriptionResponse fromEntity(com.backoffice.fitandflex.entity.ClassSubscription subscription) {
            return SubscriptionResponse.builder()
                    .id(subscription.getId())
                    .userId(subscription.getUser() != null ? subscription.getUser().getId() : null)
                    .classId(subscription.getClazz() != null ? subscription.getClazz().getId() : null)
                    .className(subscription.getClazz() != null ? subscription.getClazz().getName() : null)
                    .branch(subscription.getClazz() != null && subscription.getClazz().getBranch() != null ? 
                           BranchDto.Response.fromEntity(subscription.getClazz().getBranch()) : null)
                    .startTime(subscription.getStartTime())
                    .endTime(subscription.getEndTime())
                    .date(subscription.getDate())
                    .dayOfWeek(subscription.getDayOfWeek())
                    .recurrent(subscription.getRecurrent())
                    .active(subscription.getActive())
                    .createdAt(subscription.getCreatedAt())
                    .updatedAt(subscription.getUpdatedAt())
                    .build();
        }
    }
}
