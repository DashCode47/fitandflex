package com.backoffice.fitandflex.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean enabled;
    private BranchDto.Response branch;
    private Set<RoleDTO> roles;
}
