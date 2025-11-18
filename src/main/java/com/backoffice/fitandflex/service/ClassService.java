package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.ClassDTO;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.entity.Class;
import com.backoffice.fitandflex.entity.ClassSchedulePattern;
import com.backoffice.fitandflex.entity.Schedule;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.BranchRepository;
import com.backoffice.fitandflex.repository.ClassRepository;
import com.backoffice.fitandflex.repository.ClassSchedulePatternRepository;
import com.backoffice.fitandflex.repository.ClassSubscriptionRepository;
import com.backoffice.fitandflex.repository.ScheduleRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Servicio para gestión de clases
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassService {

    private final ClassRepository classRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ClassSchedulePatternRepository schedulePatternRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClassSubscriptionRepository subscriptionRepository;

    /**
     * Crear una nueva clase
     */
    public ClassDTO.Response createClass(ClassDTO.CreateRequest request, String createdByEmail) {
        log.info("Creando nueva clase: {}", request.getName());
        
        // Validar que la sucursal existe
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + request.getBranchId()));

        // Obtener el usuario creador
        User createdBy = userRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + createdByEmail));

        // Crear clase
        Class clazz = Class.builder()
                .name(request.getName())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .active(request.getActive() != null ? request.getActive() : true)
                .branch(branch)
                .createdBy(createdBy)
                .build();

        // Crear patrones de horarios si se proporcionaron (antes de guardar para que el cascade funcione)
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            createSchedulePatternsForClass(clazz, request.getSchedules());
            log.info("Patrones de horarios agregados a la clase: {}", clazz.getName());
        }
        
        Class savedClass = classRepository.save(clazz);
        log.info("Clase creada exitosamente: {}", savedClass.getName());
        
        // Cargar patrones para la respuesta (sin modificar la colección de la entidad)
        List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(savedClass.getId());
        
        // Contar suscripciones activas (optimizado con COUNT en BD)
        Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(savedClass.getId()).intValue();
        
        return ClassDTO.Response.fromEntity(savedClass, patterns, subscriptionCount, subscriptionRepository);
    }
    
    /**
     * Crea patrones de horarios recurrentes para una clase basándose en los días de la semana y rangos de horas
     */
    private void createSchedulePatternsForClass(Class clazz, List<ClassDTO.DaySchedule> daySchedules) {
        log.info("Creando patrones de horarios para la clase {} con {} días configurados", clazz.getName(), daySchedules.size());
        
        List<ClassSchedulePattern> patternsToCreate = new ArrayList<>();
        
        for (ClassDTO.DaySchedule daySchedule : daySchedules) {
            // Validar día de la semana (1=Lunes, 7=Domingo)
            if (daySchedule.getDayOfWeek() < 1 || daySchedule.getDayOfWeek() > 7) {
                throw new IllegalArgumentException("El día de la semana debe estar entre 1 (Lunes) y 7 (Domingo)");
            }
            
            // Validar y procesar cada rango de horas
            for (ClassDTO.TimeRange timeRange : daySchedule.getTimeRanges()) {
                // Validar que la hora de fin sea posterior a la de inicio
                if (timeRange.getEndTime().isBefore(timeRange.getStartTime()) || 
                    timeRange.getEndTime().equals(timeRange.getStartTime())) {
                    throw new IllegalArgumentException(
                        String.format("La hora de fin (%s) debe ser posterior a la hora de inicio (%s) para el día %d",
                            timeRange.getEndTime(), timeRange.getStartTime(), daySchedule.getDayOfWeek()));
                }
                
                // Crear patrón de horario
                ClassSchedulePattern pattern = ClassSchedulePattern.builder()
                        .clazz(clazz)
                        .dayOfWeek(daySchedule.getDayOfWeek())
                        .startTime(timeRange.getStartTime())
                        .endTime(timeRange.getEndTime())
                        .active(true)
                        .recurrent(daySchedule.getRecurrent() != null ? daySchedule.getRecurrent() : false)
                        .build();
                
                patternsToCreate.add(pattern);
            }
        }
        
        // Agregar patrones a la colección de la entidad (el cascade los guardará cuando se guarde la clase)
        if (!patternsToCreate.isEmpty()) {
            // Asegurarse de que la colección esté inicializada
            if (clazz.getSchedulePatterns() == null) {
                clazz.setSchedulePatterns(new HashSet<>());
            }
            clazz.getSchedulePatterns().addAll(patternsToCreate);
            log.info("Agregados {} patrones de horarios a la colección de la clase {}", patternsToCreate.size(), clazz.getName());
        }
    }

    /**
     * Obtener clase por ID
     */
    @Transactional(readOnly = true)
    public ClassDTO.Response getClassById(Long id) {
        log.info("Buscando clase por ID: {}", id);
        
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + id));
        
        // Cargar patrones para la respuesta (sin modificar la colección de la entidad)
        List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(id);
        
        // Contar suscripciones activas (optimizado con COUNT en BD)
        Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(id).intValue();
        
        return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
    }

    /**
     * Obtener todas las clases con paginación
     */
    @Transactional(readOnly = true)
    public Page<ClassDTO.Response> getAllClasses(Pageable pageable) {
        log.info("Obteniendo todas las clases con paginación");
        
        Page<Class> classes = classRepository.findAll(pageable);
        
        // Cargar patrones para cada clase (sin modificar la colección de la entidad)
        List<ClassDTO.Response> responses = classes.getContent().stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .collect(java.util.stream.Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(responses, pageable, classes.getTotalElements());
    }

    /**
     * Obtener clases por sucursal
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> getClassesByBranch(Long branchId) {
        log.info("Obteniendo clases de la sucursal: {}", branchId);
        
        List<Class> classes = classRepository.findByBranchId(branchId);
        return classes.stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .toList();
    }

    /**
     * Obtener clases activas por sucursal
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> getActiveClassesByBranch(Long branchId) {
        log.info("Obteniendo clases activas de la sucursal: {}", branchId);
        
        List<Class> classes = classRepository.findActiveClassesByBranchOrderedByName(branchId);
        return classes.stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .toList();
    }

    /**
     * Obtener clases activas
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> getActiveClasses() {
        log.info("Obteniendo clases activas");
        
        List<Class> classes = classRepository.findByActiveTrue();
        return classes.stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .toList();
    }

    /**
     * Buscar clases por nombre
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> searchClassesByName(String name) {
        log.info("Buscando clases por nombre: {}", name);
        
        List<Class> classes = classRepository.findByNameContainingIgnoreCase(name);
        return classes.stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .toList();
    }

    /**
     * Buscar clases por capacidad
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> getClassesByCapacity(Integer minCapacity, Integer maxCapacity) {
        log.info("Buscando clases por capacidad: {} - {}", minCapacity, maxCapacity);
        
        List<Class> classes;
        if (minCapacity != null && maxCapacity != null) {
            classes = classRepository.findByCapacityBetween(minCapacity, maxCapacity);
        } else if (minCapacity != null) {
            classes = classRepository.findByCapacityGreaterThanEqual(minCapacity);
        } else if (maxCapacity != null) {
            classes = classRepository.findByCapacityLessThanEqual(maxCapacity);
        } else {
            classes = classRepository.findAll();
        }
        
        return classes.stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .toList();
    }

    /**
     * Obtener clases con horarios disponibles
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> getClassesWithAvailableSchedules() {
        log.info("Obteniendo clases con horarios disponibles");
        
        List<Class> classes = classRepository.findClassesWithAvailableSchedules();
        return classes.stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .toList();
    }

    /**
     * Actualizar clase
     */
    public ClassDTO.Response updateClass(Long id, ClassDTO.UpdateRequest request) {
        log.info("Actualizando clase: {}", id);
        
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + id));

        // Actualizar campos si se proporcionan
        if (request.getName() != null) {
            clazz.setName(request.getName());
        }
        if (request.getDescription() != null) {
            clazz.setDescription(request.getDescription());
        }
        if (request.getCapacity() != null) {
            clazz.setCapacity(request.getCapacity());
        }
        if (request.getActive() != null) {
            clazz.setActive(request.getActive());
        }

        // Actualizar patrones de horarios si se proporcionaron (antes de guardar)
        if (request.getSchedules() != null) {
            updateSchedulePatternsForClass(clazz, request.getSchedules());
            log.info("Patrones de horarios actualizados para la clase: {}", clazz.getName());
        }
        
        Class updatedClass = classRepository.save(clazz);
        log.info("Clase actualizada exitosamente: {}", updatedClass.getName());
        
        // Cargar patrones para la respuesta (sin modificar la colección de la entidad)
        List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(updatedClass.getId());
        
        // Contar suscripciones activas (optimizado con COUNT en BD)
        Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(updatedClass.getId()).intValue();
        
        return ClassDTO.Response.fromEntity(updatedClass, patterns, subscriptionCount, subscriptionRepository);
    }

    /**
     * Actualiza los patrones de horarios de una clase eliminando los existentes y creando nuevos
     */
    private void updateSchedulePatternsForClass(Class clazz, List<ClassDTO.DaySchedule> daySchedules) {
        log.info("Actualizando patrones de horarios para la clase {} con {} días configurados", clazz.getName(), daySchedules.size());
        
        // Cargar los patrones existentes desde la BD
        List<ClassSchedulePattern> existingPatterns = schedulePatternRepository.findByClazzId(clazz.getId());
        
        // Inicializar la colección si es null
        if (clazz.getSchedulePatterns() == null) {
            clazz.setSchedulePatterns(new HashSet<>());
        }
        
        // Agregar los patrones existentes a la colección para que JPA los rastree
        if (!existingPatterns.isEmpty()) {
            clazz.getSchedulePatterns().addAll(existingPatterns);
            log.info("Cargados {} patrones existentes en la colección de la entidad", existingPatterns.size());
        }
        
        // Limpiar la colección (esto activará orphanRemoval cuando se guarde la entidad)
        // Los patrones existentes se eliminarán automáticamente por orphanRemoval
        clazz.getSchedulePatterns().clear();
        log.info("Colección de patrones limpiada - los patrones existentes se eliminarán al guardar");
        
        // Crear nuevos patrones si se proporcionaron
        if (!daySchedules.isEmpty()) {
            createSchedulePatternsForClass(clazz, daySchedules);
        }
    }

    /**
     * Desactivar clase
     */
    public void deactivateClass(Long id) {
        log.info("Desactivando clase: {}", id);
        
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + id));
        
        clazz.setActive(false);
        classRepository.save(clazz);
        
        log.info("Clase desactivada exitosamente: {}", clazz.getName());
    }

    /**
     * Activar clase
     */
    public void activateClass(Long id) {
        log.info("Activando clase: {}", id);
        
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + id));
        
        clazz.setActive(true);
        classRepository.save(clazz);
        
        log.info("Clase activada exitosamente: {}", clazz.getName());
    }

    /**
     * Eliminar clase (soft delete)
     */
    public void deleteClass(Long id) {
        log.info("Eliminando clase: {}", id);
        
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + id));
        
        // Soft delete - solo desactivar
        clazz.setActive(false);
        classRepository.save(clazz);
        
        log.info("Clase eliminada exitosamente: {}", clazz.getName());
    }

    /**
     * Contar clases activas por sucursal
     */
    @Transactional(readOnly = true)
    public Long countActiveClassesByBranch(Long branchId) {
        log.info("Contando clases activas de la sucursal: {}", branchId);
        
        return classRepository.countActiveClassesByBranch(branchId);
    }

    /**
     * Verificar si una clase existe
     */
    @Transactional(readOnly = true)
    public boolean classExists(Long id) {
        return classRepository.existsById(id);
    }

    /**
     * Verificar si una clase está activa
     */
    @Transactional(readOnly = true)
    public boolean isClassActive(Long id) {
        return classRepository.findById(id)
                .map(Class::getActive)
                .orElse(false);
    }

    /**
     * Obtener clases por creador
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> getClassesByCreator(Long createdById) {
        log.info("Obteniendo clases creadas por usuario: {}", createdById);
        
        List<Class> classes = classRepository.findByCreatedById(createdById);
        return classes.stream()
                .map(clazz -> {
                    List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(clazz.getId());
                    Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(clazz.getId()).intValue();
                    return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
                })
                .toList();
    }

    /**
     * Asignar día de la semana desde una fecha del calendario
     * Si es recurrente, crea un patrón de horario recurrente
     * Si no es recurrente, crea un horario específico para esa fecha
     */
    public ClassDTO.Response assignDayFromDate(Long classId, ClassDTO.AssignDayFromDateRequest request) {
        log.info("Asignando día desde fecha para clase {}: fecha={}, recurrente={}", 
                classId, request.getDate(), request.getRecurrent());
        
        // Validar que la clase existe
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + classId));

        // Validar que la clase está activa
        if (!clazz.getActive()) {
            throw new IllegalArgumentException("No se puede asignar horarios a clases inactivas");
        }

        // Validar que la hora de fin sea posterior a la de inicio
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Determinar el día de la semana de la fecha (1=Lunes, 7=Domingo)
        // Java DayOfWeek: MONDAY=1, SUNDAY=7
        int dayOfWeek = request.getDate().getDayOfWeek().getValue();

        boolean isRecurrent = request.getRecurrent() != null && request.getRecurrent();

        if (isRecurrent) {
            // Crear patrón recurrente
            log.info("Creando patrón recurrente para día {} de la semana", dayOfWeek);
            
            // Verificar si ya existe un patrón para este día y rango de horas
            List<ClassSchedulePattern> existingPatterns = schedulePatternRepository.findByClazzId(classId);
            boolean patternExists = existingPatterns.stream()
                    .anyMatch(p -> p.getDayOfWeek().equals(dayOfWeek) &&
                                  p.getStartTime().equals(request.getStartTime()) &&
                                  p.getEndTime().equals(request.getEndTime()) &&
                                  p.getActive());

            if (patternExists) {
                throw new IllegalArgumentException(
                    String.format("Ya existe un patrón recurrente para el día %d con el mismo rango de horas", dayOfWeek));
            }

            // Crear nuevo patrón recurrente
            ClassSchedulePattern pattern = ClassSchedulePattern.builder()
                    .clazz(clazz)
                    .dayOfWeek(dayOfWeek)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .active(true)
                    .recurrent(true)
                    .build();

            // Asegurarse de que la colección esté inicializada
            if (clazz.getSchedulePatterns() == null) {
                clazz.setSchedulePatterns(new HashSet<>());
            }
            clazz.getSchedulePatterns().add(pattern);
            
            classRepository.save(clazz);
            log.info("Patrón recurrente creado exitosamente para día {}", dayOfWeek);
        } else {
            // Crear horario específico para esa fecha
            log.info("Creando horario específico para fecha {}", request.getDate());
            
            // Combinar fecha y hora para crear LocalDateTime
            java.time.LocalDateTime startDateTime = request.getDate().atTime(request.getStartTime());
            java.time.LocalDateTime endDateTime = request.getDate().atTime(request.getEndTime());

            // Validar que no hay conflictos de horarios para la misma clase en esa fecha/hora
            List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(
                    classId, 
                    startDateTime, 
                    endDateTime, 
                    -1L // No excluir ningún horario para nueva creación
            );
            
            if (!conflictingSchedules.isEmpty()) {
                throw new IllegalArgumentException("Ya existe un horario para esta clase en el rango de tiempo especificado");
            }

            // Crear horario específico
            Schedule schedule = Schedule.builder()
                    .startTime(startDateTime)
                    .endTime(endDateTime)
                    .active(true)
                    .clazz(clazz)
                    .build();

            scheduleRepository.save(schedule);
            log.info("Horario específico creado exitosamente para fecha {}", request.getDate());
        }

        // Cargar patrones actualizados para la respuesta
        List<ClassSchedulePattern> patterns = schedulePatternRepository.findByClazzIdAndActiveTrue(classId);
        
        // Contar suscripciones activas (optimizado con COUNT en BD)
        Integer subscriptionCount = subscriptionRepository.countActiveSubscriptionsByClassId(classId).intValue();
        
        return ClassDTO.Response.fromEntity(clazz, patterns, subscriptionCount, subscriptionRepository);
    }
}
