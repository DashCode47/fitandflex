package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    /**
     * Buscar horarios por rango de fechas
     */
    List<Schedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Buscar horarios por clase
     */
    List<Schedule> findByClazzId(Long classId);
    
    /**
     * Buscar horarios activos
     */
    List<Schedule> findByActiveTrue();
    
    /**
     * Buscar horarios inactivos
     */
    List<Schedule> findByActiveFalse();
    
    /**
     * Buscar horarios activos por clase
     */
    List<Schedule> findByClazzIdAndActiveTrue(Long classId);
    
    /**
     * Buscar horarios por fecha específica
     */
    @Query("SELECT s FROM Schedule s WHERE DATE(s.startTime) = DATE(:date) AND s.active = true")
    List<Schedule> findByDate(@Param("date") LocalDateTime date);
    
    /**
     * Buscar horarios futuros
     */
    @Query("SELECT s FROM Schedule s WHERE s.startTime > :now AND s.active = true ORDER BY s.startTime ASC")
    List<Schedule> findFutureSchedules(@Param("now") LocalDateTime now);
    
    /**
     * Buscar horarios pasados
     */
    @Query("SELECT s FROM Schedule s WHERE s.endTime < :now ORDER BY s.startTime DESC")
    List<Schedule> findPastSchedules(@Param("now") LocalDateTime now);
    
    /**
     * Buscar horarios disponibles (con cupos disponibles)
     */
    @Query("SELECT s FROM Schedule s WHERE s.active = true AND s.startTime > :now AND " +
           "s.clazz.capacity > (SELECT COUNT(r) FROM Reservation r WHERE r.schedule = s AND r.status = 'ACTIVE') " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findAvailableSchedules(@Param("now") LocalDateTime now);
    
    /**
     * Buscar horarios por sucursal
     */
    @Query("SELECT s FROM Schedule s WHERE s.clazz.branch.id = :branchId AND s.active = true ORDER BY s.startTime ASC")
    List<Schedule> findByBranchId(@Param("branchId") Long branchId);
    
    /**
     * Buscar horarios por clase y rango de fechas
     */
    @Query("SELECT s FROM Schedule s WHERE s.clazz.id = :classId AND s.startTime BETWEEN :start AND :end AND s.active = true ORDER BY s.startTime ASC")
    List<Schedule> findByClassIdAndDateRange(@Param("classId") Long classId, 
                                           @Param("start") LocalDateTime start, 
                                           @Param("end") LocalDateTime end);
    
    /**
     * Buscar horarios con conflictos de tiempo
     */
    @Query("SELECT s FROM Schedule s WHERE s.clazz.id = :classId AND s.id != :excludeId AND s.active = true AND " +
           "((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findConflictingSchedules(@Param("classId") Long classId, 
                                          @Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime, 
                                          @Param("excludeId") Long excludeId);
    
    /**
     * Contar horarios activos por clase
     */
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.clazz.id = :classId AND s.active = true")
    Long countActiveSchedulesByClass(@Param("classId") Long classId);
    
    /**
     * Buscar horarios por día de la semana
     */
    @Query("SELECT s FROM Schedule s WHERE DAYOFWEEK(s.startTime) = :dayOfWeek AND s.active = true ORDER BY s.startTime ASC")
    List<Schedule> findByDayOfWeek(@Param("dayOfWeek") Integer dayOfWeek);
    
    /**
     * Buscar horarios próximos (próximos 7 días)
     */
    @Query("SELECT s FROM Schedule s WHERE s.startTime BETWEEN :now AND :nextWeek AND s.active = true ORDER BY s.startTime ASC")
    List<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now, @Param("nextWeek") LocalDateTime nextWeek);
}
