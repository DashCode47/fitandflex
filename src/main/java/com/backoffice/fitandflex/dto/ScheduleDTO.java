package com.backoffice.fitandflex.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleDTO {
    private Long id;
    private ClassDTO yogaClass;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
}
