package com.backoffice.fitandflex.config;

import com.backoffice.fitandflex.entity.Role;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.RoleRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

/**
 * Inicializador de datos para crear roles y usuario SUPER_ADMIN por defecto
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initializeData() {
        log.info("Inicializando datos del sistema...");
        
        // Crear roles si no existen
        createRolesIfNotExist();
        
        // Crear SUPER_ADMIN si no existe
        createSuperAdminIfNotExists();
        
        log.info("Inicialización de datos completada");
    }

    private void createRolesIfNotExist() {
        String[] roleNames = {"SUPER_ADMIN", "BRANCH_ADMIN", "USER", "INSTRUCTOR"};
        
        for (String roleName : roleNames) {
            if (!roleRepository.findByName(roleName).isPresent()) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getRoleDescription(roleName))
                        .build();
                
                roleRepository.save(role);
                log.info("Rol creado: {}", roleName);
            }
        }
    }

    private void createSuperAdminIfNotExists() {
        String superAdminEmail = "admin@fitandflex.com";
        
        if (!userRepository.existsByEmail(superAdminEmail)) {
            Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol SUPER_ADMIN no encontrado"));
            
            User superAdmin = User.builder()
                    .name("Super Administrador")
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .phone("+1234567890")
                    .gender("M")
                    .active(true)
                    .role(superAdminRole)
                    .build();
            
            userRepository.save(superAdmin);
            log.info("Usuario SUPER_ADMIN creado: {}", superAdminEmail);
        } else {
            log.info("Usuario SUPER_ADMIN ya existe");
        }
    }

    private String getRoleDescription(String roleName) {
        return switch (roleName) {
            case "SUPER_ADMIN" -> "Administrador del sistema con acceso completo";
            case "BRANCH_ADMIN" -> "Administrador de sucursal con acceso limitado a su sucursal";
            case "USER" -> "Usuario regular del sistema";
            case "INSTRUCTOR" -> "Instructor de clases";
            default -> "Rol del sistema";
        };
    }
}
