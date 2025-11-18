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
     */
    public ClassDTO.SubscriptionResponse createSubscription(Long classId, ClassDTO.CreateSubscriptionRequest request) {
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

        // Validar que el rango de horas es válido
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // Validar lógica de recurrent y date
        Boolean recurrent = request.getRecurrent() != null ? request.getRecurrent() : false;
        LocalDate date = request.getDate();

        if (recurrent && date != null) {
            throw new IllegalArgumentException("Una suscripción recurrente no puede tener una fecha específica");
        }

        if (!recurrent && date == null) {
            throw new IllegalArgumentException("Una suscripción no recurrente debe tener una fecha específica");
        }

        // Calcular día de la semana antes de validar duplicados
        Integer dayOfWeek;
        if (request.getDayOfWeek() != null) {
            dayOfWeek = request.getDayOfWeek();
        } else if (date != null) {
            dayOfWeek = date.getDayOfWeek().getValue();
        } else if (recurrent) {
            throw new IllegalArgumentException("Para suscripciones recurrentes, se debe proporcionar dayOfWeek o date para calcularlo");
        } else {
            throw new IllegalArgumentException("Se debe proporcionar date o dayOfWeek para crear la suscripción");
        }

        // Validar que no existe ya una suscripción activa con los mismos datos
        boolean exists;
        if (recurrent) {
            // Para suscripciones recurrentes, verificar si existe una con date IS NULL y mismo día
            exists = subscriptionRepository.existsActiveRecurrentSubscription(
                    request.getUserId(), 
                    classId, 
                    dayOfWeek,
                    request.getStartTime(), 
                    request.getEndTime());
        } else {
            // Para suscripciones específicas, verificar si existe una con la fecha específica y mismo día
            exists = subscriptionRepository.existsActiveSubscriptionWithDate(
                    request.getUserId(), 
                    classId, 
                    dayOfWeek,
                    date, 
                    request.getStartTime(), 
                    request.getEndTime());
        }
        
        if (exists) {
            throw new IllegalArgumentException("Ya existe una suscripción activa para este usuario, clase, fecha y horario");
        }

        // Validar capacidad de la clase (si es fecha específica)
        if (!recurrent && date != null) {
            Long currentSubscriptions = subscriptionRepository.countByClazzIdAndDateAndStartTimeAndEndTimeAndActiveTrue(
                    classId, date, request.getStartTime(), request.getEndTime());
            
            if (currentSubscriptions >= clazz.getCapacity()) {
                throw new IllegalArgumentException("La clase está llena para este horario y fecha");
            }
        }

        // Crear la suscripción
        // Si es recurrente, date debe ser NULL; si no, usar la fecha específica
        LocalDate subscriptionDate = recurrent ? null : date;
        
        ClassSubscription subscription = ClassSubscription.builder()
                .user(user)
                .clazz(clazz)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .date(subscriptionDate)
                .dayOfWeek(dayOfWeek)
                .recurrent(recurrent)
                .active(true)
                .build();

        ClassSubscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Suscripción creada exitosamente con ID: {}", savedSubscription.getId());

        return ClassDTO.SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Obtener todas las suscripciones activas de una clase
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.SubscriptionResponse> getSubscriptionsByClassId(Long classId) {
        log.info("Obteniendo suscripciones activas de la clase: {}", classId);
        
        List<ClassSubscription> subscriptions = subscriptionRepository.findByClazzIdAndActiveTrue(classId);
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
}

