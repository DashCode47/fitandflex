package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    /**
     * Buscar sucursal por nombre
     */
    Optional<Branch> findByName(String name);
    
    /**
     * Verificar si existe una sucursal con el nombre dado
     */
    boolean existsByName(String name);
    
    /**
     * Buscar sucursales por ciudad (case insensitive)
     */
    List<Branch> findByCityIgnoreCase(String city);
}
