package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.ClassDTO;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.entity.Class;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.BranchRepository;
import com.backoffice.fitandflex.repository.ClassRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Class savedClass = classRepository.save(clazz);
        log.info("Clase creada exitosamente: {}", savedClass.getName());
        
        return ClassDTO.Response.fromEntity(savedClass);
    }

    /**
     * Obtener clase por ID
     */
    @Transactional(readOnly = true)
    public ClassDTO.Response getClassById(Long id) {
        log.info("Buscando clase por ID: {}", id);
        
        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada: " + id));
        
        return ClassDTO.Response.fromEntity(clazz);
    }

    /**
     * Obtener todas las clases con paginación
     */
    @Transactional(readOnly = true)
    public Page<ClassDTO.Response> getAllClasses(Pageable pageable) {
        log.info("Obteniendo todas las clases con paginación");
        
        Page<Class> classes = classRepository.findAll(pageable);
        return classes.map(ClassDTO.Response::fromEntity);
    }

    /**
     * Obtener clases por sucursal
     */
    @Transactional(readOnly = true)
    public List<ClassDTO.Response> getClassesByBranch(Long branchId) {
        log.info("Obteniendo clases de la sucursal: {}", branchId);
        
        List<Class> classes = classRepository.findByBranchId(branchId);
        return classes.stream()
                .map(ClassDTO.Response::fromEntity)
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
                .map(ClassDTO.Response::fromEntity)
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
                .map(ClassDTO.Response::fromEntity)
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
                .map(ClassDTO.Response::fromEntity)
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
                .map(ClassDTO.Response::fromEntity)
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
                .map(ClassDTO.Response::fromEntity)
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

        Class updatedClass = classRepository.save(clazz);
        log.info("Clase actualizada exitosamente: {}", updatedClass.getName());
        
        return ClassDTO.Response.fromEntity(updatedClass);
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
                .map(ClassDTO.Response::fromEntity)
                .toList();
    }
}
