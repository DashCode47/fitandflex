package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.UserMembershipDTO;
import com.backoffice.fitandflex.entity.UserMembership;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.entity.Product;
import com.backoffice.fitandflex.repository.UserMembershipRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import com.backoffice.fitandflex.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de membresías de usuario
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserMembershipService {

    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Asignar membresía a usuario
     */
    public UserMembershipDTO.Response assignMembership(UserMembershipDTO.CreateRequest request, String assignedByEmail) {
        log.info("Asignando membresía {} al usuario {}", request.getProductId(), request.getUserId());

        // Validar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + request.getUserId()));

        // Validar que el producto existe
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + request.getProductId()));

        // Validar que el producto está activo
        if (!product.getActive()) {
            throw new IllegalArgumentException("No se puede asignar un producto inactivo");
        }

        // Validar fechas
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Obtener quien asigna la membresía
        User assignedBy = userRepository.findByEmail(assignedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario que asigna no encontrado: " + assignedByEmail));

        // Verificar si ya existe una membresía activa del mismo producto
        if (userMembershipRepository.findActiveByUserIdAndProductId(request.getUserId(), request.getProductId()).isPresent()) {
            throw new IllegalArgumentException("El usuario ya tiene una membresía activa de este producto");
        }

        // Crear la membresía
        UserMembership membership = UserMembership.builder()
                .user(user)
                .product(product)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status("ACTIVE")
                .active(true)
                .notes(request.getNotes())
                .assignedBy(assignedBy)
                .build();

        UserMembership savedMembership = userMembershipRepository.save(membership);
        log.info("Membresía asignada exitosamente con ID: {}", savedMembership.getId());

        return UserMembershipDTO.Response.fromEntity(savedMembership);
    }

    /**
     * Obtener todas las membresías con paginación
     */
    @Transactional(readOnly = true)
    public Page<UserMembershipDTO.Response> getAllMemberships(Pageable pageable) {
        log.info("Obteniendo todas las membresías con paginación: {}", pageable);
        
        Page<UserMembership> memberships = userMembershipRepository.findAll(pageable);
        return memberships.map(UserMembershipDTO.Response::fromEntity);
    }

    /**
     * Obtener membresía por ID
     */
    @Transactional(readOnly = true)
    public UserMembershipDTO.Response getMembershipById(Long id) {
        log.info("Obteniendo membresía con ID: {}", id);
        
        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada: " + id));
        
        return UserMembershipDTO.Response.fromEntity(membership);
    }

    /**
     * Obtener membresías por usuario
     */
    @Transactional(readOnly = true)
    public List<UserMembershipDTO.Response> getMembershipsByUser(Long userId) {
        log.info("Obteniendo membresías del usuario: {}", userId);
        
        // Validar que el usuario existe
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        List<UserMembership> memberships = userMembershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener membresías activas por usuario
     */
    @Transactional(readOnly = true)
    public List<UserMembershipDTO.Response> getActiveMembershipsByUser(Long userId) {
        log.info("Obteniendo membresías activas del usuario: {}", userId);
        
        // Validar que el usuario existe
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        List<UserMembership> memberships = userMembershipRepository.findActiveMembershipsByUser(userId, LocalDateTime.now());
        return memberships.stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener membresías por sucursal
     */
    @Transactional(readOnly = true)
    public List<UserMembershipDTO.Response> getMembershipsByBranch(Long branchId) {
        log.info("Obteniendo membresías de la sucursal: {}", branchId);
        
        List<UserMembership> memberships = userMembershipRepository.findByBranchId(branchId);
        return memberships.stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener membresías activas por sucursal
     */
    @Transactional(readOnly = true)
    public List<UserMembershipDTO.Response> getActiveMembershipsByBranch(Long branchId) {
        log.info("Obteniendo membresías activas de la sucursal: {}", branchId);
        
        List<UserMembership> memberships = userMembershipRepository.findActiveByBranchId(branchId);
        return memberships.stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener membresías que expiran pronto
     */
    @Transactional(readOnly = true)
    public List<UserMembershipDTO.Response> getExpiringMemberships(int days) {
        log.info("Obteniendo membresías que expiran en los próximos {} días", days);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        
        List<UserMembership> memberships = userMembershipRepository.findExpiringSoon(now, futureDate);
        return memberships.stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener membresías vencidas
     */
    @Transactional(readOnly = true)
    public List<UserMembershipDTO.Response> getExpiredMemberships() {
        log.info("Obteniendo membresías vencidas");
        
        List<UserMembership> memberships = userMembershipRepository.findExpired(LocalDateTime.now());
        return memberships.stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar membresía
     */
    public UserMembershipDTO.Response updateMembership(Long id, UserMembershipDTO.UpdateRequest request) {
        log.info("Actualizando membresía: {}", id);
        
        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada: " + id));
        
        // Actualizar campos si se proporcionan
        if (request.getStartDate() != null) {
            membership.setStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null) {
            membership.setEndDate(request.getEndDate());
        }
        
        if (request.getStatus() != null) {
            membership.setStatus(request.getStatus());
        }
        
        if (request.getActive() != null) {
            membership.setActive(request.getActive());
        }
        
        if (request.getNotes() != null) {
            membership.setNotes(request.getNotes());
        }
        
        // Validar fechas si se actualizaron
        if (membership.getStartDate().isAfter(membership.getEndDate())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        UserMembership updatedMembership = userMembershipRepository.save(membership);
        log.info("Membresía actualizada exitosamente: {}", updatedMembership.getId());
        
        return UserMembershipDTO.Response.fromEntity(updatedMembership);
    }

    /**
     * Cambiar estado de membresía
     */
    public UserMembershipDTO.Response changeMembershipStatus(Long id, UserMembershipDTO.ChangeStatusRequest request) {
        log.info("Cambiando estado de membresía {} a {}", id, request.getStatus());
        
        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada: " + id));
        
        // Cambiar estado según el tipo
        switch (request.getStatus().toUpperCase()) {
            case "ACTIVE":
                membership.activate();
                break;
            case "CANCELLED":
                membership.cancel();
                break;
            case "SUSPENDED":
                membership.suspend();
                break;
            case "EXPIRED":
                membership.expire();
                break;
            default:
                throw new IllegalArgumentException("Estado inválido: " + request.getStatus());
        }
        
        // Actualizar notas si se proporciona razón
        if (request.getReason() != null) {
            String currentNotes = membership.getNotes() != null ? membership.getNotes() : "";
            membership.setNotes(currentNotes + "\n" + request.getReason());
        }
        
        UserMembership updatedMembership = userMembershipRepository.save(membership);
        log.info("Estado de membresía cambiado exitosamente: {}", updatedMembership.getId());
        
        return UserMembershipDTO.Response.fromEntity(updatedMembership);
    }

    /**
     * Extender membresía
     */
    public UserMembershipDTO.Response extendMembership(Long id, UserMembershipDTO.ExtendRequest request) {
        log.info("Extendiendo membresía {} por {} días", id, request.getAdditionalDays());
        
        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada: " + id));
        
        // Extender la fecha de fin
        LocalDateTime newEndDate = membership.getEndDate().plusDays(request.getAdditionalDays());
        membership.setEndDate(newEndDate);
        
        // Actualizar notas
        if (request.getReason() != null) {
            String currentNotes = membership.getNotes() != null ? membership.getNotes() : "";
            membership.setNotes(currentNotes + "\nExtendida por " + request.getAdditionalDays() + " días: " + request.getReason());
        }
        
        UserMembership updatedMembership = userMembershipRepository.save(membership);
        log.info("Membresía extendida exitosamente: {}", updatedMembership.getId());
        
        return UserMembershipDTO.Response.fromEntity(updatedMembership);
    }

    /**
     * Eliminar membresía
     */
    public void deleteMembership(Long id) {
        log.info("Eliminando membresía: {}", id);
        
        UserMembership membership = userMembershipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada: " + id));
        
        userMembershipRepository.delete(membership);
        log.info("Membresía eliminada exitosamente: {}", id);
    }

    /**
     * Verificar si usuario tiene membresía activa
     */
    @Transactional(readOnly = true)
    public boolean hasActiveMembership(Long userId) {
        return userMembershipRepository.hasActiveMembership(userId, LocalDateTime.now());
    }

    /**
     * Obtener resumen de membresías por usuario
     */
    @Transactional(readOnly = true)
    public List<UserMembershipDTO.SummaryResponse> getMembershipSummaryByUser(Long userId) {
        log.info("Obteniendo resumen de membresías del usuario: {}", userId);
        
        List<UserMembership> memberships = userMembershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(UserMembershipDTO.SummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
