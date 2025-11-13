package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.ClassSchedulePattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSchedulePatternRepository extends JpaRepository<ClassSchedulePattern, Long> {
    
    /**
     * Buscar patrones por clase
     */
    List<ClassSchedulePattern> findByClazzId(Long classId);
    
    /**
     * Buscar patrones activos por clase
     */
    List<ClassSchedulePattern> findByClazzIdAndActiveTrue(Long classId);
    
    /**
     * Buscar patrones activos
     */
    List<ClassSchedulePattern> findByActiveTrue();
    
    /**
     * Buscar patrones por día de la semana
     */
    List<ClassSchedulePattern> findByDayOfWeekAndActiveTrue(Integer dayOfWeek);
    
    /**
     * Eliminar todos los patrones de una clase
     */
    void deleteByClazzId(Long classId);
}

