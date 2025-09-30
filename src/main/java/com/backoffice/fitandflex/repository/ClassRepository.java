package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassRepository extends JpaRepository<Class, Long> {
    
    /**
     * Buscar clases por sucursal
     */
    List<Class> findByBranchId(Long branchId);
    
    /**
     * Buscar clases activas
     */
    List<Class> findByActiveTrue();
    
    /**
     * Buscar clases inactivas
     */
    List<Class> findByActiveFalse();
    
    /**
     * Buscar clases activas por sucursal
     */
    List<Class> findByBranchIdAndActiveTrue(Long branchId);
    
    /**
     * Buscar clases por nombre (case insensitive)
     */
    List<Class> findByNameContainingIgnoreCase(String name);
    
    /**
     * Buscar clases por capacidad mínima
     */
    List<Class> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    /**
     * Buscar clases por capacidad máxima
     */
    List<Class> findByCapacityLessThanEqual(Integer maxCapacity);
    
    /**
     * Buscar clases por rango de capacidad
     */
    List<Class> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
    
    /**
     * Buscar clases por creador
     */
    List<Class> findByCreatedById(Long createdById);
    
    /**
     * Buscar clases activas por sucursal con ordenamiento
     */
    @Query("SELECT c FROM Class c WHERE c.branch.id = :branchId AND c.active = true ORDER BY c.name ASC")
    List<Class> findActiveClassesByBranchOrderedByName(@Param("branchId") Long branchId);
    
    /**
     * Contar clases activas por sucursal
     */
    @Query("SELECT COUNT(c) FROM Class c WHERE c.branch.id = :branchId AND c.active = true")
    Long countActiveClassesByBranch(@Param("branchId") Long branchId);
    
    /**
     * Buscar clases con horarios disponibles
     */
    @Query("SELECT DISTINCT c FROM Class c LEFT JOIN c.schedules s WHERE c.active = true AND (s IS NULL OR s.active = true)")
    List<Class> findClassesWithAvailableSchedules();
}
