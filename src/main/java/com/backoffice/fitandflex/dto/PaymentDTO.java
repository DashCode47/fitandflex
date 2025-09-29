package com.backoffice.fitandflex.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private UserDTO user;
    private ProductDTO product;
    private Double amount;
    private String method;
    private LocalDateTime paymentDate;
}
