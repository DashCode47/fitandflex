package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Reservation;
import com.backoffice.fitandflex.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de reservas
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Buscar reservas por usuario
     */
    List<Reservation> findByUserId(Long userId);

    /**
     * Buscar reservas por usuario con paginación
     */
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    /**
     * Buscar reservas por horario
     */
    List<Reservation> findByScheduleId(Long scheduleId);

    /**
     * Buscar reservas por horario con paginación
     */
    Page<Reservation> findByScheduleId(Long scheduleId, Pageable pageable);

    /**
     * Buscar reservas por estado
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Buscar reservas por estado con paginación
     */
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    /**
     * Buscar reservas por usuario y estado
     */
    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    /**
     * Buscar reservas por usuario y estado con paginación
     */
    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    /**
     * Buscar reservas por horario y estado
     */
    List<Reservation> findByScheduleIdAndStatus(Long scheduleId, ReservationStatus status);

    /**
     * Verificar si existe una reserva para un usuario y horario específicos
     */
    boolean existsByUserIdAndScheduleId(Long userId, Long scheduleId);

    /**
     * Buscar reserva específica por usuario y horario
     */
    Optional<Reservation> findByUserIdAndScheduleId(Long userId, Long scheduleId);

    /**
     * Buscar reservas activas por usuario
     */
    List<Reservation> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ReservationStatus status);

    /**
     * Buscar reservas activas por usuario con paginación
     */
    Page<Reservation> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ReservationStatus status, Pageable pageable);

    /**
     * Buscar reservas por rango de fechas
     */
    List<Reservation> findByReservationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Buscar reservas por rango de fechas con paginación
     */
    Page<Reservation> findByReservationDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Buscar reservas por usuario y rango de fechas
     */
    List<Reservation> findByUserIdAndReservationDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Buscar reservas por usuario y rango de fechas con paginación
     */
    Page<Reservation> findByUserIdAndReservationDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Contar reservas por usuario
     */
    long countByUserId(Long userId);

    /**
     * Contar reservas por usuario y estado
     */
    long countByUserIdAndStatus(Long userId, ReservationStatus status);

    /**
     * Contar reservas por horario
     */
    long countByScheduleId(Long scheduleId);


    /**
     * Buscar reservas por sucursal (a través del horario)
     * Usa JOIN FETCH para cargar relaciones y evitar problemas de lazy loading
     */
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "JOIN FETCH r.schedule s " +
           "JOIN FETCH s.clazz c " +
           "JOIN FETCH c.branch b " +
           "WHERE b.id = :branchId")
    List<Reservation> findByBranchId(@Param("branchId") Long branchId);

    /**
     * Buscar reservas por sucursal con paginación
     * Nota: No se puede usar JOIN FETCH con Page, así que usamos JOIN normal
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "JOIN s.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId")
    Page<Reservation> findByBranchId(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar reservas por sucursal y estado
     */
    @Query("SELECT DISTINCT r FROM Reservation r " +
           "JOIN FETCH r.schedule s " +
           "JOIN FETCH s.clazz c " +
           "JOIN FETCH c.branch b " +
           "WHERE b.id = :branchId AND r.status = :status")
    List<Reservation> findByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") ReservationStatus status);

    /**
     * Buscar reservas por sucursal y estado con paginación
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "JOIN s.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId AND r.status = :status")
    Page<Reservation> findByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") ReservationStatus status, Pageable pageable);

    /**
     * Buscar reservas futuras por usuario
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "WHERE r.user.id = :userId AND s.startTime > :now AND r.status = 'ACTIVE'")
    List<Reservation> findFutureReservationsByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Buscar reservas pasadas por usuario
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "WHERE r.user.id = :userId AND s.startTime <= :now")
    List<Reservation> findPastReservationsByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Buscar reservas por clase (a través del horario)
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "WHERE s.clazz.id = :classId")
    List<Reservation> findByClassId(@Param("classId") Long classId);

    /**
     * Buscar reservas por clase con paginación
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "WHERE s.clazz.id = :classId")
    Page<Reservation> findByClassId(@Param("classId") Long classId, Pageable pageable);

    /**
     * Buscar reservas por clase y estado
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "WHERE s.clazz.id = :classId AND r.status = :status")
    List<Reservation> findByClassIdAndStatus(@Param("classId") Long classId, @Param("status") ReservationStatus status);

    /**
     * Buscar reservas por clase y estado con paginación
     */
    @Query("SELECT r FROM Reservation r " +
           "JOIN r.schedule s " +
           "WHERE s.clazz.id = :classId AND r.status = :status")
    Page<Reservation> findByClassIdAndStatus(@Param("classId") Long classId, @Param("status") ReservationStatus status, Pageable pageable);

    /**
     * Buscar reservas activas por horario
     */
    List<Reservation> findByScheduleIdAndStatusOrderByCreatedAtAsc(Long scheduleId, ReservationStatus status);

    /**
     * Contar reservas activas por horario
     */
    long countByScheduleIdAndStatus(Long scheduleId, ReservationStatus status);

    /**
     * Buscar reservas por estado y rango de fechas
     */
    List<Reservation> findByStatusAndReservationDateBetween(ReservationStatus status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Buscar reservas por estado y rango de fechas con paginación
     */
    Page<Reservation> findByStatusAndReservationDateBetween(ReservationStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Contar reservas por sucursal (para debugging)
     */
    @Query("SELECT COUNT(r) FROM Reservation r " +
           "JOIN r.schedule s " +
           "JOIN s.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId")
    long countByBranchId(@Param("branchId") Long branchId);
}