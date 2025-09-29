package com.backoffice.fitandflex.dto;

import lombok.Data;

@Data
public class ClassDTO {
    private Long id;
    private String name;
    private String description;
    private BranchDto.Response branch;
}
