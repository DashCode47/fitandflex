package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.ClassSubscription;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.entity.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSubscriptionRepository extends JpaRepository<ClassSubscription, Long> {

    /**
     * Obtener todas las suscripciones activas de un usuario
     */
    List<ClassSubscription> findByUserIdAndActiveTrue(Long userId);

    /**
     * Obtener todas las suscripciones activas de una clase
     */
    List<ClassSubscription> findByClazzIdAndActiveTrue(Long classId);

    /**
     * Obtener todas las suscripciones de un usuario (activas e inactivas)
     */
    List<ClassSubscription> findByUserId(Long userId);

    /**
     * Obtener todas las suscripciones de una clase (activas e inactivas)
     */
    List<ClassSubscription> findByClazzId(Long classId);

    /**
     * Verificar si existe una suscripción activa específica para fecha específica
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END " +
           "FROM ClassSubscription cs WHERE cs.user.id = :userId AND cs.clazz.id = :classId AND " +
           "cs.dayOfWeek = :dayOfWeek AND cs.date = :date AND cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true")
    boolean existsActiveSubscriptionWithDate(
            @Param("userId") Long userId,
            @Param("classId") Long classId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Buscar una suscripción (activa o inactiva) por usuario, clase, fecha y horario
     * Útil para reactivar suscripciones canceladas
     */
    @Query("SELECT cs FROM ClassSubscription cs WHERE cs.user.id = :userId AND cs.clazz.id = :classId AND " +
           "cs.dayOfWeek = :dayOfWeek AND cs.date = :date AND cs.startTime = :startTime AND cs.endTime = :endTime")
    Optional<ClassSubscription> findSubscriptionByUserClassDateAndTime(
            @Param("userId") Long userId,
            @Param("classId") Long classId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Verificar si existe una suscripción activa recurrente (sin fecha específica) para un día específico
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END " +
           "FROM ClassSubscription cs WHERE cs.user.id = :userId AND cs.clazz.id = :classId AND " +
           "cs.dayOfWeek = :dayOfWeek AND cs.date IS NULL AND cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true")
    boolean existsActiveRecurrentSubscription(
            @Param("userId") Long userId,
            @Param("classId") Long classId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Buscar una suscripción activa específica (para casos donde se necesita el objeto completo)
     * Maneja correctamente los casos donde date puede ser NULL (suscripciones recurrentes)
     */
    @Query("SELECT cs FROM ClassSubscription cs WHERE cs.user.id = :userId AND cs.clazz.id = :classId AND " +
           "cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true AND " +
           "(:date IS NULL AND cs.date IS NULL OR :date IS NOT NULL AND cs.date = :date)")
    Optional<ClassSubscription> findActiveSubscription(
            @Param("userId") Long userId,
            @Param("classId") Long classId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Buscar suscripciones activas para una clase en un horario específico
     */
    @Query("SELECT cs FROM ClassSubscription cs WHERE cs.clazz.id = :classId AND cs.date = :date AND " +
           "cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true")
    List<ClassSubscription> findActiveSubscriptionsForClassAndTime(
            @Param("classId") Long classId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Obtener usuarios únicos activos de una clase
     */
    @Query("SELECT DISTINCT cs.user FROM ClassSubscription cs WHERE cs.clazz.id = :classId AND cs.active = true")
    List<User> findActiveUsersByClassId(@Param("classId") Long classId);

    /**
     * Obtener usuarios únicos activos de una clase para un horario específico
     * Incluye tanto suscripciones recurrentes como específicas que coinciden con el rango de horas
     */
    @Query("SELECT DISTINCT cs.user FROM ClassSubscription cs WHERE cs.clazz.id = :classId AND " +
           "cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true")
    List<User> findActiveUsersByClassAndTimeRange(
            @Param("classId") Long classId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Obtener usuarios únicos activos de una clase para un horario y fecha específica
     */
    @Query("SELECT DISTINCT cs.user FROM ClassSubscription cs WHERE cs.clazz.id = :classId AND " +
           "cs.date = :date AND cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true")
    List<User> findActiveUsersByClassTimeAndDate(
            @Param("classId") Long classId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Obtener clases únicas activas de un usuario
     */
    @Query("SELECT DISTINCT cs.clazz FROM ClassSubscription cs WHERE cs.user.id = :userId AND cs.active = true")
    List<Class> findActiveClassesByUserId(@Param("userId") Long userId);

    /**
     * Contar suscripciones activas para una clase en un horario específico
     */
    Long countByClazzIdAndDateAndStartTimeAndEndTimeAndActiveTrue(
            Long classId, 
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime);

    /**
     * Contar todas las suscripciones activas de una clase (optimizado)
     */
    @Query("SELECT COUNT(cs) FROM ClassSubscription cs WHERE cs.clazz.id = :classId AND cs.active = true")
    Long countActiveSubscriptionsByClassId(@Param("classId") Long classId);

    /**
     * Contar suscripciones activas para un horario específico de una clase y día de la semana
     * Cuenta tanto suscripciones recurrentes como específicas que coinciden con el día y rango de horas
     */
    @Query("SELECT COUNT(cs) FROM ClassSubscription cs WHERE cs.clazz.id = :classId AND " +
           "cs.dayOfWeek = :dayOfWeek AND cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true")
    Long countActiveSubscriptionsByClassDayAndTimeRange(
            @Param("classId") Long classId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Obtener clases únicas que tienen suscripciones activas para una fecha específica
     */
    @Query("SELECT DISTINCT cs.clazz FROM ClassSubscription cs WHERE cs.date = :date AND cs.active = true")
    List<Class> findClassesWithSubscriptionsForDate(@Param("date") LocalDate date);

    /**
     * Obtener suscripciones activas de una clase para una fecha específica
     */
    @Query("SELECT cs FROM ClassSubscription cs WHERE cs.clazz.id = :classId AND cs.date = :date AND cs.active = true")
    List<ClassSubscription> findSubscriptionsByClassAndDate(
            @Param("classId") Long classId,
            @Param("date") LocalDate date);

    /**
     * Buscar una suscripción activa por usuario, clase y fecha
     * Si hay múltiples suscripciones para la misma fecha, retorna la primera encontrada
     */
    @Query("SELECT cs FROM ClassSubscription cs WHERE cs.user.id = :userId AND cs.clazz.id = :classId AND " +
           "cs.date = :date AND cs.active = true ORDER BY cs.startTime ASC")
    List<ClassSubscription> findActiveSubscriptionsByUserClassAndDate(
            @Param("userId") Long userId,
            @Param("classId") Long classId,
            @Param("date") LocalDate date);

    /**
     * Buscar una suscripción activa por usuario, clase, fecha y horario específico
     */
    @Query("SELECT cs FROM ClassSubscription cs WHERE cs.user.id = :userId AND cs.clazz.id = :classId AND " +
           "cs.date = :date AND cs.startTime = :startTime AND cs.endTime = :endTime AND cs.active = true")
    Optional<ClassSubscription> findActiveSubscriptionByUserClassDateAndTime(
            @Param("userId") Long userId,
            @Param("classId") Long classId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Buscar suscripciones por sucursal (a través de la clase)
     * Carga las relaciones necesarias para evitar LazyInitializationException
     */
    @EntityGraph(attributePaths = {"user", "clazz", "clazz.branch"})
    @Query("SELECT DISTINCT cs FROM ClassSubscription cs " +
           "JOIN cs.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId")
    List<ClassSubscription> findByBranchId(@Param("branchId") Long branchId);

    /**
     * Buscar suscripciones por sucursal con paginación
     */
    @Query("SELECT cs FROM ClassSubscription cs " +
           "JOIN cs.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId")
    Page<ClassSubscription> findByBranchId(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar suscripciones activas por sucursal
     */
    @EntityGraph(attributePaths = {"user", "clazz", "clazz.branch"})
    @Query("SELECT DISTINCT cs FROM ClassSubscription cs " +
           "JOIN cs.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId AND cs.active = true")
    List<ClassSubscription> findByBranchIdAndActiveTrue(@Param("branchId") Long branchId);

    /**
     * Buscar suscripciones activas por sucursal con paginación
     */
    @Query("SELECT cs FROM ClassSubscription cs " +
           "JOIN cs.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId AND cs.active = true")
    Page<ClassSubscription> findByBranchIdAndActiveTrue(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Contar suscripciones por sucursal
     */
    @Query("SELECT COUNT(cs) FROM ClassSubscription cs " +
           "JOIN cs.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId")
    long countByBranchId(@Param("branchId") Long branchId);

    /**
     * Contar suscripciones activas por sucursal
     */
    @Query("SELECT COUNT(cs) FROM ClassSubscription cs " +
           "JOIN cs.clazz c " +
           "JOIN c.branch b " +
           "WHERE b.id = :branchId AND cs.active = true")
    long countByBranchIdAndActiveTrue(@Param("branchId") Long branchId);
}

