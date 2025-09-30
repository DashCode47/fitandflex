package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.UserDTO;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.entity.Role;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.BranchRepository;
import com.backoffice.fitandflex.repository.RoleRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crear un nuevo usuario
     */
    public UserDTO.Response createUser(UserDTO.CreateRequest request) {
        log.info("Creando nuevo usuario: {}", request.getEmail());
        
        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
        }

        // Buscar rol
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + request.getRoleName()));

        // Buscar sucursal si se proporciona
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + request.getBranchId()));
        }

        // Crear usuario
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .gender(request.getGender())
                .active(request.getActive() != null ? request.getActive() : true)
                .role(role)
                .branch(branch)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario creado exitosamente: {}", savedUser.getEmail());
        
        return UserDTO.Response.fromEntity(savedUser);
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UserDTO.Response getUserById(Long id) {
        log.info("Buscando usuario por ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        return UserDTO.Response.fromEntity(user);
    }

    /**
     * Obtener usuario por email
     */
    @Transactional(readOnly = true)
    public UserDTO.Response getUserByEmail(String email) {
        log.info("Buscando usuario por email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        
        return UserDTO.Response.fromEntity(user);
    }

    /**
     * Obtener todos los usuarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<UserDTO.Response> getAllUsers(Pageable pageable) {
        log.info("Obteniendo todos los usuarios con paginación");
        
        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserDTO.Response::fromEntity);
    }

    /**
     * Obtener usuarios por sucursal
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> getUsersByBranch(Long branchId) {
        log.info("Obteniendo usuarios de la sucursal: {}", branchId);
        
        List<User> users = userRepository.findByBranchId(branchId);
        return users.stream()
                .map(UserDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> getUsersByRole(String roleName) {
        log.info("Obteniendo usuarios por rol: {}", roleName);
        
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(UserDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Actualizar usuario
     */
    public UserDTO.Response updateUser(Long id, UserDTO.UpdateRequest request) {
        log.info("Actualizando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        // Actualizar campos si se proporcionan
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Validar que el nuevo email no exista
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + request.getRoleName()));
            user.setRole(role);
        }
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + request.getBranchId()));
            user.setBranch(branch);
        }

        User updatedUser = userRepository.save(user);
        log.info("Usuario actualizado exitosamente: {}", updatedUser.getEmail());
        
        return UserDTO.Response.fromEntity(updatedUser);
    }

    /**
     * Cambiar contraseña
     */
    public void changePassword(Long id, String newPassword) {
        log.info("Cambiando contraseña para usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Contraseña cambiada exitosamente para usuario: {}", user.getEmail());
    }

    /**
     * Desactivar usuario
     */
    public void deactivateUser(Long id) {
        log.info("Desactivando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Usuario desactivado exitosamente: {}", user.getEmail());
    }

    /**
     * Activar usuario
     */
    public void activateUser(Long id) {
        log.info("Activando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        user.setActive(true);
        userRepository.save(user);
        
        log.info("Usuario activado exitosamente: {}", user.getEmail());
    }

    /**
     * Eliminar usuario (soft delete)
     */
    public void deleteUser(Long id) {
        log.info("Eliminando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        // Soft delete - solo desactivar
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Usuario eliminado exitosamente: {}", user.getEmail());
    }

    /**
     * Verificar si un email existe
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Obtener usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> getActiveUsers() {
        log.info("Obteniendo usuarios activos");
        
        List<User> users = userRepository.findByActiveTrue();
        return users.stream()
                .map(UserDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Verificar si un usuario es propietario de un recurso
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long userId, String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
            return user.getId().equals(userId);
        } catch (Exception e) {
            log.warn("Error verificando propiedad del usuario: {}", e.getMessage());
            return false;
        }
    }
}
