package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.ClassDTO;
import com.backoffice.fitandflex.entity.Class;
import com.backoffice.fitandflex.entity.ClassSubscription;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.ClassRepository;
import com.backoffice.fitandflex.repository.ClassSubscriptionRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de suscripciones de usuarios a clases
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassSubscriptionService {

    private final ClassSubscriptionRepository subscriptionRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    /**
     * Crear una nueva suscripción
     * @param classId ID de la clase
     * @param request Datos de la suscripción
     * @param userBranchId ID de la sucursal del usuario (null si es SUPER_ADMIN)
     * @param isSuperAdmin Indica si el usuario es SUPER_ADMIN
     */
    public ClassDTO.SubscriptionResponse createSubscription(Long classId, ClassDTO.CreateSubscriptionRequest request, Long userBranchId, boolean isSuperAdmin) {
        log.info("Creando suscripción para usuario {} en clase {}", request.getUserId(), classId);

        // Validar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + request.getUserId()));

        // Validar que la clase existe
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada con ID: " + classId));

        // Validar que la clase está activa
        if (!clazz.getActive()) {
            throw new IllegalArgumentException("No se puede suscribir a clases inactivas");
        }

        // Validar que BRANCH_ADMIN solo puede crear suscripciones para clases de su branch
        if (!isSuperAdmin && userBranchId != null && clazz.getBranch() != null) {
            if (!clazz.getBranch().getId().equals(userBranchId)) {
                throw new IllegalArgumentException("No se puede crear suscripciones para clases de otras sucursales");
            }
        }

        // Validar que el rango de horas es válido
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // Validar que la fecha es obligatoria
        LocalDate date = request.getDate();
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria para crear una suscripción");
        }

        // Calcular día de la semana desde la fecha
        Integer dayOfWeek = date.getDayOfWeek().getValue();

        // Verificar si existe una suscripción activa con los mismos datos
        boolean existsActive = subscriptionRepository.existsActiveSubscriptionWithDate(
                request.getUserId(), 
                classId, 
                dayOfWeek,
                date, 
                request.getStartTime(), 
                request.getEndTime());
        
        if (existsActive) {
            throw new IllegalArgumentException("Ya existe una suscripción activa para este usuario, clase, fecha y horario");
        }

        // Verificar si existe una suscripción inactiva (cancelada previamente) con los mismos datos
        // Si existe, reactivarla en lugar de crear una nueva
        Optional<ClassSubscription> existingInactiveSubscription = subscriptionRepository.findSubscriptionByUserClassDateAndTime(
                request.getUserId(),
                classId,
                dayOfWeek,
                date,
                request.getStartTime(),
                request.getEndTime());

        ClassSubscription savedSubscription;
        
        if (existingInactiveSubscription.isPresent() && !existingInactiveSubscription.get().getActive()) {
            // Reactivar suscripción existente
            ClassSubscription subscription = existingInactiveSubscription.get();
            subscription.setActive(true);
            savedSubscription = subscriptionRepository.save(subscription);
            log.info("Suscripción reactivada exitosamente con ID: {}", savedSubscription.getId());
        } else {
            // Validar capacidad de la clase para esta fecha específica
            Long currentSubscriptions = subscriptionRepository.countByClazzIdAndDateAndStartTimeAndEndTimeAndActiveTrue(
                    classId, date, request.getStartTime(), request.getEndTime());
            
            if (currentSubscriptions >= clazz.getCapacity()) {
                throw new IllegalArgumentException("La clase está llena para este horario y fecha");
            }

            // Crear nueva suscripción (siempre con fecha específica, recurrent siempre false)
            ClassSubscription subscription = ClassSubscription.builder()
                    .user(user)
                    .clazz(clazz)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .date(date)
                    .dayOfWeek(dayOfWeek)
                    .recurrent(false) // Siempre false, cada suscripción es para una fecha específica
                    .active(true)
                    .build();

            savedSubscription = subscriptionRepository.save(subscription);
            log.info("Suscripción creada exitosamente con ID: {}", savedSubscription.getId());
        }

        return ClassDTO.SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Obtener todas las suscripciones activas de una clase
     * @param classId ID de la clase
     * @param branchId ID de la sucursal para filtrar (null para obtener todas)
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.SubscriptionResponse> getSubscriptionsByClassId(Long classId, Long branchId) {
        log.info("Obteniendo suscripciones activas de la clase: {}, branchId: {}", classId, branchId);
        
        List<ClassSubscription> subscriptions = subscriptionRepository.findByClazzIdAndActiveTrue(classId);
        
        // Filtrar por branch si se proporciona
        if (branchId != null) {
            subscriptions = subscriptions.stream()
                    .filter(sub -> sub.getClazz() != null && 
                                 sub.getClazz().getBranch() != null && 
                                 sub.getClazz().getBranch().getId().equals(branchId))
                    .collect(Collectors.toList());
        }
        
        return subscriptions.stream()
                .map(ClassDTO.SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las suscripciones activas de un usuario
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.SubscriptionResponse> getSubscriptionsByUserId(Long userId) {
        log.info("Obteniendo suscripciones activas del usuario: {}", userId);
        
        List<ClassSubscription> subscriptions = subscriptionRepository.findByUserIdAndActiveTrue(userId);
        return subscriptions.stream()
                .map(ClassDTO.SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las clases a las que un usuario está suscrito
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.SummaryResponse> getClassesByUserId(Long userId) {
        log.info("Obteniendo clases del usuario: {}", userId);
        
        List<Class> classes = subscriptionRepository.findActiveClassesByUserId(userId);
        return classes.stream()
                .map(ClassDTO.SummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los usuarios suscritos a una clase
     */
    @Transactional(readOnly = true)
    public List<com.backoffice.fitandflex.dto.UserDTO.SummaryResponse> getUsersByClassId(Long classId) {
        log.info("Obteniendo usuarios de la clase: {}", classId);
        
        List<User> users = subscriptionRepository.findActiveUsersByClassId(classId);
        return users.stream()
                .map(com.backoffice.fitandflex.dto.UserDTO.SummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener usuarios suscritos a un horario específico de una clase
     * Incluye tanto suscripciones recurrentes como específicas que coinciden con el rango de horas
     */
    @Transactional(readOnly = true)
    public List<com.backoffice.fitandflex.dto.UserDTO.SummaryResponse> getUsersByClassAndTimeRange(
            Long classId, LocalTime startTime, LocalTime endTime) {
        log.info("Obteniendo usuarios de la clase {} para horario {} - {}", classId, startTime, endTime);
        
        List<User> users = subscriptionRepository.findActiveUsersByClassAndTimeRange(classId, startTime, endTime);
        return users.stream()
                .map(com.backoffice.fitandflex.dto.UserDTO.SummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener usuarios suscritos a un horario y fecha específica de una clase
     */
    @Transactional(readOnly = true)
    public List<com.backoffice.fitandflex.dto.UserDTO.SummaryResponse> getUsersByClassTimeAndDate(
            Long classId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        log.info("Obteniendo usuarios de la clase {} para fecha {} y horario {} - {}", classId, date, startTime, endTime);
        
        List<User> users = subscriptionRepository.findActiveUsersByClassTimeAndDate(classId, date, startTime, endTime);
        return users.stream()
                .map(com.backoffice.fitandflex.dto.UserDTO.SummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Cancelar una suscripción (marcar como inactiva)
     */
    public ClassDTO.SubscriptionResponse cancelSubscription(Long subscriptionId) {
        log.info("Cancelando suscripción: {}", subscriptionId);
        
        ClassSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada con ID: " + subscriptionId));
        
        subscription.setActive(false);
        ClassSubscription savedSubscription = subscriptionRepository.save(subscription);
        
        log.info("Suscripción cancelada exitosamente: {}", subscriptionId);
        return ClassDTO.SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Eliminar una suscripción permanentemente
     */
    public void deleteSubscription(Long subscriptionId) {
        log.info("Eliminando suscripción: {}", subscriptionId);
        
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new IllegalArgumentException("Suscripción no encontrada con ID: " + subscriptionId);
        }
        
        subscriptionRepository.deleteById(subscriptionId);
        log.info("Suscripción eliminada exitosamente: {}", subscriptionId);
    }

    /**
     * Cancelar suscripción por usuario, clase y fecha
     * Si hay múltiples suscripciones para la misma fecha, cancela todas
     */
    public ClassDTO.SubscriptionResponse cancelSubscriptionByUserClassAndDate(
            Long userId, Long classId, LocalDate date) {
        log.info("Cancelando suscripción para usuario {}, clase {} y fecha {}", userId, classId, date);
        
        List<ClassSubscription> subscriptions = subscriptionRepository.findActiveSubscriptionsByUserClassAndDate(
                userId, classId, date);
        
        if (subscriptions.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("No se encontró una suscripción activa para el usuario %d, clase %d y fecha %s", 
                            userId, classId, date));
        }
        
        // Si hay múltiples suscripciones para la misma fecha, cancelar todas
        if (subscriptions.size() > 1) {
            log.warn("Se encontraron {} suscripciones para usuario {}, clase {} y fecha {}. Cancelando todas.", 
                    subscriptions.size(), userId, classId, date);
        }
        
        // Cancelar la primera (o todas si es necesario)
        ClassSubscription subscription = subscriptions.get(0);
        subscription.setActive(false);
        ClassSubscription savedSubscription = subscriptionRepository.save(subscription);
        
        log.info("Suscripción cancelada exitosamente: {}", savedSubscription.getId());
        return ClassDTO.SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Cancelar suscripción por usuario, clase, fecha y horario específico
     * Permite cancelar una suscripción específica cuando hay múltiples para la misma fecha
     */
    public ClassDTO.SubscriptionResponse cancelSubscriptionByUserClassDateAndTime(
            Long userId, Long classId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        log.info("Cancelando suscripción para usuario {}, clase {}, fecha {} y horario {} - {}", 
                userId, classId, date, startTime, endTime);
        
        ClassSubscription subscription = subscriptionRepository.findActiveSubscriptionByUserClassDateAndTime(
                userId, classId, date, startTime, endTime)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No se encontró una suscripción activa para el usuario %d, clase %d, fecha %s y horario %s - %s", 
                                userId, classId, date, startTime, endTime)));
        
        subscription.setActive(false);
        ClassSubscription savedSubscription = subscriptionRepository.save(subscription);
        
        log.info("Suscripción cancelada exitosamente: {}", savedSubscription.getId());
        return ClassDTO.SubscriptionResponse.fromEntity(savedSubscription);
    }
}

