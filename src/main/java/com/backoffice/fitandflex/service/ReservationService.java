package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.dto.ReservationDTO;
import com.backoffice.fitandflex.entity.Reservation;
import com.backoffice.fitandflex.entity.ReservationStatus;
import com.backoffice.fitandflex.entity.Schedule;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.BranchRepository;
import com.backoffice.fitandflex.repository.ReservationRepository;
import com.backoffice.fitandflex.repository.ScheduleRepository;
import com.backoffice.fitandflex.repository.UserRepository;
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
 * Servicio para gestión de reservas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final BranchRepository branchRepository;

    /**
     * Crear una nueva reserva
     */
    public ReservationDTO.Response createReservation(ReservationDTO.CreateRequest request) {
        log.info("Creando reserva para usuario {} en horario {}", request.getUserId(), request.getScheduleId());

        // Validar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + request.getUserId()));

        // Validar que el horario existe
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + request.getScheduleId()));

        // Validar que el horario está activo
        if (!schedule.getActive()) {
            throw new IllegalArgumentException("No se puede reservar en horarios inactivos");
        }

        // Validar que el horario es futuro
        if (schedule.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede reservar en horarios pasados");
        }

        // Validar que no existe ya una reserva para este usuario y horario
        if (reservationRepository.existsByUserIdAndScheduleId(request.getUserId(), request.getScheduleId())) {
            throw new IllegalArgumentException("Ya existe una reserva para este usuario en este horario");
        }

        // Validar que el usuario no tiene una reserva activa en el mismo horario
        List<Reservation> existingReservations = reservationRepository.findByUserIdAndStatus(
                request.getUserId(), ReservationStatus.ACTIVE);
        
        for (Reservation existingReservation : existingReservations) {
            if (existingReservation.getSchedule().getStartTime().equals(schedule.getStartTime())) {
                throw new IllegalArgumentException("El usuario ya tiene una reserva activa en este horario");
            }
        }

        // Crear la reserva
        Reservation reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .status(ReservationStatus.ACTIVE)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reserva creada exitosamente con ID: {}", savedReservation.getId());

        return ReservationDTO.fromEntity(savedReservation);
    }

    /**
     * Obtener todas las reservas con paginación
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO.Response> getAllReservations(Pageable pageable) {
        log.info("Obteniendo todas las reservas con paginación: {}", pageable);
        
        Page<Reservation> reservations = reservationRepository.findAll(pageable);
        return reservations.map(ReservationDTO::fromEntity);
    }

    /**
     * Obtener resumen de todas las reservas
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO.SummaryResponse> getAllReservationsSummary() {
        log.info("Obteniendo resumen de todas las reservas");
        
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationDTO::fromEntityToSummary)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reserva por ID
     */
    @Transactional(readOnly = true)
    public ReservationDTO.Response getReservationById(Long id) {
        log.info("Obteniendo reserva con ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        
        return ReservationDTO.fromEntity(reservation);
    }

    /**
     * Obtener reservas por usuario
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO.Response> getReservationsByUser(Long userId, Pageable pageable) {
        log.info("Obteniendo reservas para usuario {} con paginación: {}", userId, pageable);
        
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }
        
        Page<Reservation> reservations = reservationRepository.findByUserId(userId, pageable);
        return reservations.map(ReservationDTO::fromEntity);
    }

    /**
     * Obtener reservas por horario
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO.Response> getReservationsBySchedule(Long scheduleId, Pageable pageable) {
        log.info("Obteniendo reservas para horario {} con paginación: {}", scheduleId, pageable);
        
        // Validar que el horario existe
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new IllegalArgumentException("Horario no encontrado con ID: " + scheduleId);
        }
        
        Page<Reservation> reservations = reservationRepository.findByScheduleId(scheduleId, pageable);
        return reservations.map(ReservationDTO::fromEntity);
    }

    /**
     * Obtener reservas por estado
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO.Response> getReservationsByStatus(ReservationStatus status, Pageable pageable) {
        log.info("Obteniendo reservas con estado {} con paginación: {}", status, pageable);
        
        Page<Reservation> reservations = reservationRepository.findByStatus(status, pageable);
        return reservations.map(ReservationDTO::fromEntity);
    }

    /**
     * Obtener reservas por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO.Response> getReservationsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo reservas para sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        // Contar total para debugging
        long totalCount = reservationRepository.countByBranchId(branchId);
        log.debug("Total de reservas encontradas para branch {}: {}", branchId, totalCount);
        
        Page<Reservation> reservations = reservationRepository.findByBranchId(branchId, pageable);
        log.info("Reservas obtenidas: {} de {}", reservations.getNumberOfElements(), reservations.getTotalElements());
        
        return reservations.map(ReservationDTO::fromEntity);
    }

    /**
     * Obtener reservas por clase
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO.Response> getReservationsByClass(Long classId, Pageable pageable) {
        log.info("Obteniendo reservas para clase {} con paginación: {}", classId, pageable);
        
        Page<Reservation> reservations = reservationRepository.findByClassId(classId, pageable);
        return reservations.map(ReservationDTO::fromEntity);
    }

    /**
     * Obtener reservas futuras por usuario
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO.Response> getFutureReservationsByUser(Long userId) {
        log.info("Obteniendo reservas futuras para usuario: {}", userId);
        
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }
        
        List<Reservation> reservations = reservationRepository.findFutureReservationsByUser(userId, LocalDateTime.now());
        return reservations.stream()
                .map(ReservationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reservas pasadas por usuario
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO.Response> getPastReservationsByUser(Long userId) {
        log.info("Obteniendo reservas pasadas para usuario: {}", userId);
        
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }
        
        List<Reservation> reservations = reservationRepository.findPastReservationsByUser(userId, LocalDateTime.now());
        return reservations.stream()
                .map(ReservationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar reserva
     */
    public ReservationDTO.Response updateReservation(Long id, ReservationDTO.UpdateRequest request) {
        log.info("Actualizando reserva con ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        
        // Validar que se puede actualizar
        if (reservation.getStatus() == ReservationStatus.ATTENDED) {
            throw new IllegalArgumentException("No se puede modificar una reserva ya atendida");
        }
        
        // Actualizar estado si se proporciona
        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }
        
        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Reserva actualizada exitosamente con ID: {}", updatedReservation.getId());
        
        return ReservationDTO.fromEntity(updatedReservation);
    }

    /**
     * Cancelar reserva
     */
    public ReservationDTO.Response cancelReservation(Long id) {
        log.info("Cancelando reserva con ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        
        // Validar que se puede cancelar
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Solo se pueden cancelar reservas activas");
        }
        
        // Validar que el horario es futuro
        if (reservation.getSchedule().getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede cancelar una reserva de un horario pasado");
        }
        
        reservation.setStatus(ReservationStatus.CANCELED);
        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Reserva cancelada exitosamente con ID: {}", updatedReservation.getId());
        
        return ReservationDTO.fromEntity(updatedReservation);
    }

    /**
     * Marcar asistencia
     */
    public ReservationDTO.Response markAttendance(Long id) {
        log.info("Marcando asistencia para reserva con ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        
        // Validar que se puede marcar asistencia
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Solo se puede marcar asistencia en reservas activas");
        }
        
        reservation.setStatus(ReservationStatus.ATTENDED);
        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Asistencia marcada exitosamente para reserva con ID: {}", updatedReservation.getId());
        
        return ReservationDTO.fromEntity(updatedReservation);
    }

    /**
     * Marcar no asistencia
     */
    public ReservationDTO.Response markNoShow(Long id) {
        log.info("Marcando no asistencia para reserva con ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        
        // Validar que se puede marcar no asistencia
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Solo se puede marcar no asistencia en reservas activas");
        }
        
        reservation.setStatus(ReservationStatus.NO_SHOW);
        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("No asistencia marcada exitosamente para reserva con ID: {}", updatedReservation.getId());
        
        return ReservationDTO.fromEntity(updatedReservation);
    }

    /**
     * Eliminar reserva (soft delete)
     */
    public void deleteReservation(Long id) {
        log.info("Eliminando reserva con ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
        
        // Validar que se puede eliminar
        if (reservation.getStatus() == ReservationStatus.ATTENDED) {
            throw new IllegalArgumentException("No se puede eliminar una reserva ya atendida");
        }
        
        reservationRepository.delete(reservation);
        log.info("Reserva eliminada exitosamente con ID: {}", id);
    }

    /**
     * Verificar si existe una reserva
     */
    @Transactional(readOnly = true)
    public boolean existsReservation(Long userId, Long scheduleId) {
        return reservationRepository.existsByUserIdAndScheduleId(userId, scheduleId);
    }

    /**
     * Obtener estadísticas de reservas
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getReservationStats() {
        long totalReservations = reservationRepository.count();
        
        // Contar reservas por estado usando consultas existentes
        List<Reservation> activeReservations = reservationRepository.findByStatus(ReservationStatus.ACTIVE);
        List<Reservation> canceledReservations = reservationRepository.findByStatus(ReservationStatus.CANCELED);
        List<Reservation> attendedReservations = reservationRepository.findByStatus(ReservationStatus.ATTENDED);
        List<Reservation> noShowReservations = reservationRepository.findByStatus(ReservationStatus.NO_SHOW);
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(java.util.Map.of(
                    "totalReservations", totalReservations,
                    "activeReservations", activeReservations.size(),
                    "canceledReservations", canceledReservations.size(),
                    "attendedReservations", attendedReservations.size(),
                    "noShowReservations", noShowReservations.size()
                ))
                .build();
    }

    /**
     * Obtener estadísticas de reservas por usuario
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getReservationStatsByUser(Long userId) {
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }
        
        long totalReservations = reservationRepository.countByUserId(userId);
        
        // Contar reservas por estado usando consultas existentes
        List<Reservation> activeReservations = reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.ACTIVE);
        List<Reservation> canceledReservations = reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.CANCELED);
        List<Reservation> attendedReservations = reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.ATTENDED);
        List<Reservation> noShowReservations = reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.NO_SHOW);
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas del usuario obtenidas exitosamente")
                .data(java.util.Map.of(
                    "userId", userId,
                    "totalReservations", totalReservations,
                    "activeReservations", activeReservations.size(),
                    "canceledReservations", canceledReservations.size(),
                    "attendedReservations", attendedReservations.size(),
                    "noShowReservations", noShowReservations.size()
                ))
                .build();
    }
}
