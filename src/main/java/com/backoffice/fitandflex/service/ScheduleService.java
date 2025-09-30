package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.ScheduleDTO;
import com.backoffice.fitandflex.entity.Class;
import com.backoffice.fitandflex.entity.Schedule;
import com.backoffice.fitandflex.repository.ClassRepository;
import com.backoffice.fitandflex.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestión de horarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassRepository classRepository;

    /**
     * Crear un nuevo horario
     */
    public ScheduleDTO.Response createSchedule(ScheduleDTO.CreateRequest request) {
        log.info("Creando nuevo horario para clase: {}", request.getClassId());
        
        // Validar que la clase existe
        Class clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + request.getClassId()));

        // Validar que la clase está activa
        if (!clazz.getActive()) {
            throw new IllegalArgumentException("No se puede crear horarios para clases inactivas");
        }

        // Validar que endTime es posterior a startTime
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().isEqual(request.getStartTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar que no hay conflictos de horarios para la misma clase
        List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(
                request.getClassId(), 
                request.getStartTime(), 
                request.getEndTime(), 
                -1L // No excluir ningún horario para nueva creación
        );
        
        if (!conflictingSchedules.isEmpty()) {
            throw new IllegalArgumentException("Ya existe un horario para esta clase en el rango de tiempo especificado");
        }

        // Crear horario
        Schedule schedule = Schedule.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(request.getActive() != null ? request.getActive() : true)
                .clazz(clazz)
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("Horario creado exitosamente: {} - {}", savedSchedule.getStartTime(), savedSchedule.getEndTime());
        
        return ScheduleDTO.Response.fromEntity(savedSchedule);
    }

    /**
     * Obtener horario por ID
     */
    @Transactional(readOnly = true)
    public ScheduleDTO.Response getScheduleById(Long id) {
        log.info("Buscando horario por ID: {}", id);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + id));
        
        return ScheduleDTO.Response.fromEntity(schedule);
    }

    /**
     * Obtener todos los horarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<ScheduleDTO.Response> getAllSchedules(Pageable pageable) {
        log.info("Obteniendo todos los horarios con paginación");
        
        Page<Schedule> schedules = scheduleRepository.findAll(pageable);
        return schedules.map(ScheduleDTO.Response::fromEntity);
    }

    /**
     * Obtener horarios por clase
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getSchedulesByClass(Long classId) {
        log.info("Obteniendo horarios de la clase: {}", classId);
        
        List<Schedule> schedules = scheduleRepository.findByClazzId(classId);
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios activos por clase
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getActiveSchedulesByClass(Long classId) {
        log.info("Obteniendo horarios activos de la clase: {}", classId);
        
        List<Schedule> schedules = scheduleRepository.findByClazzIdAndActiveTrue(classId);
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios activos
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getActiveSchedules() {
        log.info("Obteniendo horarios activos");
        
        List<Schedule> schedules = scheduleRepository.findByActiveTrue();
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios futuros
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getFutureSchedules() {
        log.info("Obteniendo horarios futuros");
        
        List<Schedule> schedules = scheduleRepository.findFutureSchedules(LocalDateTime.now());
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios disponibles (con cupos disponibles)
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getAvailableSchedules() {
        log.info("Obteniendo horarios disponibles");
        
        List<Schedule> schedules = scheduleRepository.findAvailableSchedules(LocalDateTime.now());
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios por sucursal
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getSchedulesByBranch(Long branchId) {
        log.info("Obteniendo horarios de la sucursal: {}", branchId);
        
        List<Schedule> schedules = scheduleRepository.findByBranchId(branchId);
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getSchedulesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Obteniendo horarios por rango de fechas: {} - {}", startDate, endDate);
        
        List<Schedule> schedules = scheduleRepository.findByStartTimeBetween(startDate, endDate);
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios por fecha específica
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getSchedulesByDate(LocalDateTime date) {
        log.info("Obteniendo horarios por fecha: {}", date);
        
        List<Schedule> schedules = scheduleRepository.findByDate(date);
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios próximos (próximos 7 días)
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getUpcomingSchedules() {
        log.info("Obteniendo horarios próximos");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);
        
        List<Schedule> schedules = scheduleRepository.findUpcomingSchedules(now, nextWeek);
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener horarios por día de la semana
     */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> getSchedulesByDayOfWeek(Integer dayOfWeek) {
        log.info("Obteniendo horarios por día de la semana: {}", dayOfWeek);
        
        List<Schedule> schedules = scheduleRepository.findByDayOfWeek(dayOfWeek);
        return schedules.stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Actualizar horario
     */
    public ScheduleDTO.Response updateSchedule(Long id, ScheduleDTO.UpdateRequest request) {
        log.info("Actualizando horario: {}", id);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + id));

        // Validar que endTime es posterior a startTime si se proporcionan ambos
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : schedule.getStartTime();
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : schedule.getEndTime();
        
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar conflictos si se cambian las fechas
        if (request.getStartTime() != null || request.getEndTime() != null) {
            List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(
                    schedule.getClazz().getId(), 
                    startTime, 
                    endTime, 
                    id // Excluir el horario actual
            );
            
            if (!conflictingSchedules.isEmpty()) {
                throw new IllegalArgumentException("Ya existe un horario para esta clase en el rango de tiempo especificado");
            }
        }

        // Actualizar campos si se proporcionan
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getActive() != null) {
            schedule.setActive(request.getActive());
        }

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("Horario actualizado exitosamente: {} - {}", updatedSchedule.getStartTime(), updatedSchedule.getEndTime());
        
        return ScheduleDTO.Response.fromEntity(updatedSchedule);
    }

    /**
     * Desactivar horario
     */
    public void deactivateSchedule(Long id) {
        log.info("Desactivando horario: {}", id);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + id));
        
        schedule.setActive(false);
        scheduleRepository.save(schedule);
        
        log.info("Horario desactivado exitosamente: {} - {}", schedule.getStartTime(), schedule.getEndTime());
    }

    /**
     * Activar horario
     */
    public void activateSchedule(Long id) {
        log.info("Activando horario: {}", id);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + id));
        
        schedule.setActive(true);
        scheduleRepository.save(schedule);
        
        log.info("Horario activado exitosamente: {} - {}", schedule.getStartTime(), schedule.getEndTime());
    }

    /**
     * Eliminar horario (soft delete)
     */
    public void deleteSchedule(Long id) {
        log.info("Eliminando horario: {}", id);
        
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + id));
        
        // Soft delete - solo desactivar
        schedule.setActive(false);
        scheduleRepository.save(schedule);
        
        log.info("Horario eliminado exitosamente: {} - {}", schedule.getStartTime(), schedule.getEndTime());
    }

    /**
     * Contar horarios activos por clase
     */
    @Transactional(readOnly = true)
    public Long countActiveSchedulesByClass(Long classId) {
        log.info("Contando horarios activos de la clase: {}", classId);
        
        return scheduleRepository.countActiveSchedulesByClass(classId);
    }

    /**
     * Verificar si un horario existe
     */
    @Transactional(readOnly = true)
    public boolean scheduleExists(Long id) {
        return scheduleRepository.existsById(id);
    }

    /**
     * Verificar si un horario está activo
     */
    @Transactional(readOnly = true)
    public boolean isScheduleActive(Long id) {
        return scheduleRepository.findById(id)
                .map(Schedule::getActive)
                .orElse(false);
    }

    /**
     * Verificar si un horario tiene cupos disponibles
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableSpots(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + id));
        
        int reservationCount = schedule.getReservations() != null ? schedule.getReservations().size() : 0;
        int capacity = schedule.getClazz() != null ? schedule.getClazz().getCapacity() : 0;
        
        return capacity > reservationCount;
    }

    /**
     * Obtener cupos disponibles de un horario
     */
    @Transactional(readOnly = true)
    public Integer getAvailableSpots(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + id));
        
        int reservationCount = schedule.getReservations() != null ? schedule.getReservations().size() : 0;
        int capacity = schedule.getClazz() != null ? schedule.getClazz().getCapacity() : 0;
        
        return Math.max(0, capacity - reservationCount);
    }
}
