package com.backoffice.fitandflex.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    private Long id;
    private UserDTO user;
    private ScheduleDTO schedule;
    private LocalDateTime reservationDate;
    private String status;
}
