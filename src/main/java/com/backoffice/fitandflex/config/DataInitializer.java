package com.backoffice.fitandflex.config;

import com.backoffice.fitandflex.entity.Role;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.RoleRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.admin.email:admin@fitandflex.com}")
    private String adminEmail;

    @Value("${app.admin.password:#{null}}")
    private String adminPassword;

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
        if (!userRepository.existsByEmail(adminEmail)) {
            // En producción, ADMIN_PASSWORD debe estar configurado
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("⚠️ ADMIN_PASSWORD no configurado. Usuario SUPER_ADMIN no será creado automáticamente.");
                log.warn("Configure las variables de entorno: ADMIN_EMAIL y ADMIN_PASSWORD");
                return;
            }

            Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol SUPER_ADMIN no encontrado"));
            
            User superAdmin = User.builder()
                    .name("Super Administrador")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .phone("+1234567890")
                    .active(true)
                    .role(superAdminRole)
                    .build();
            
            userRepository.save(superAdmin);
            log.info("✅ Usuario SUPER_ADMIN creado: {}", adminEmail);
        } else {
            log.info("Usuario SUPER_ADMIN ya existe: {}", adminEmail);
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
