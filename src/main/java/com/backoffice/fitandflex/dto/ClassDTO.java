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
        private BranchDto.Response branch;
        private UserDTO.SummaryResponse createdBy;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Lista de patrones de horarios de la clase (día de la semana y rangos de horas)")
        private List<DaySchedule> schedules;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Class clazz) {
            return fromEntity(clazz, null);
        }
        
        public static Response fromEntity(Class clazz, java.util.Collection<com.backoffice.fitandflex.entity.ClassSchedulePattern> patterns) {
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
                            .map(pattern -> TimeRange.builder()
                                .startTime(pattern.getStartTime())
                                .endTime(pattern.getEndTime())
                                .build())
                            .collect(Collectors.toList());
                        
                        return DaySchedule.builder()
                            .dayOfWeek(entry.getKey())
                            .timeRanges(timeRanges)
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
