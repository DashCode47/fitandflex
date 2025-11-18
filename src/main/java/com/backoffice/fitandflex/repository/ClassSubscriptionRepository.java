package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.ClassSubscription;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.entity.Class;
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
}

