package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.ClassDTO;
import com.backoffice.fitandflex.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de clases
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Classes", description = "Endpoints para gestión de clases")
public class ClassController {

    private final ClassService classService;
    
    /**
     * Helper method para crear Pageable con validaciones
     */
    private org.springframework.data.domain.Pageable createPageable(int page, int size, String sort, String direction) {
        // Validar parámetros
        if (size > 100) size = 100;
        if (size < 1) size = 10;
        if (page < 0) page = 0;
        
        org.springframework.data.domain.Sort.Direction sortDirection = 
            "desc".equalsIgnoreCase(direction) ? 
            org.springframework.data.domain.Sort.Direction.DESC : 
            org.springframework.data.domain.Sort.Direction.ASC;
            
        return org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sortDirection, sort));
    }

    @Operation(
        summary = "Crear nueva clase",
        description = "Crea una nueva clase en el sistema. Solo usuarios con rol SUPER_ADMIN o BRANCH_ADMIN pueden realizar esta operación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Clase creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClassDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol SUPER_ADMIN o BRANCH_ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<ClassDTO.Response> createClass(
            @Valid @RequestBody ClassDTO.CreateRequest request,
            Authentication authentication) {
        log.info("Creando nueva clase: {}", request.getName());
        
        String createdByEmail = authentication.getName();
        ClassDTO.Response response = classService.createClass(request, createdByEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Obtener clase por ID",
        description = "Obtiene la información completa de una clase por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Clase encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClassDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Clase no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClassDTO.Response> getClassById(@PathVariable Long id) {
        log.info("Obteniendo clase por ID: {}", id);
        
        ClassDTO.Response response = classService.getClassById(id);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener todas las clases",
        description = "Obtiene una lista paginada de todas las clases del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<ClassDTO.Response>> getAllClasses(
            @Parameter(description = "Número de página (por defecto: 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10, máximo: 100)") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: 'name')") 
            @RequestParam(defaultValue = "name") String sort,
            @Parameter(description = "Dirección del ordenamiento (asc/desc, por defecto: 'asc')") 
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Obteniendo todas las clases con paginación - page: {}, size: {}, sort: {}, direction: {}", 
                page, size, sort, direction);
        
        org.springframework.data.domain.Pageable pageable = createPageable(page, size, sort, direction);
        
        Page<ClassDTO.Response> response = classService.getAllClasses(pageable);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener clases por sucursal",
        description = "Obtiene todas las clases de una sucursal específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases de la sucursal obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ClassDTO.Response>> getClassesByBranch(@PathVariable Long branchId) {
        log.info("Obteniendo clases de la sucursal: {}", branchId);
        
        List<ClassDTO.Response> response = classService.getClassesByBranch(branchId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener clases activas por sucursal",
        description = "Obtiene todas las clases activas de una sucursal específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases activas de la sucursal obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/branch/{branchId}/active")
    public ResponseEntity<List<ClassDTO.Response>> getActiveClassesByBranch(@PathVariable Long branchId) {
        log.info("Obteniendo clases activas de la sucursal: {}", branchId);
        
        List<ClassDTO.Response> response = classService.getActiveClassesByBranch(branchId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener clases activas",
        description = "Obtiene todas las clases activas del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases activas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/active")
    public ResponseEntity<List<ClassDTO.Response>> getActiveClasses() {
        log.info("Obteniendo clases activas");
        
        List<ClassDTO.Response> response = classService.getActiveClasses();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Buscar clases por nombre",
        description = "Busca clases que contengan el nombre especificado (búsqueda case insensitive)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases encontradas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/search")
    public ResponseEntity<List<ClassDTO.Response>> searchClassesByName(
            @Parameter(description = "Nombre o parte del nombre de la clase") 
            @RequestParam String name) {
        log.info("Buscando clases por nombre: {}", name);
        
        List<ClassDTO.Response> response = classService.searchClassesByName(name);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Buscar clases por capacidad",
        description = "Busca clases por rango de capacidad"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases encontradas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/capacity")
    public ResponseEntity<List<ClassDTO.Response>> getClassesByCapacity(
            @Parameter(description = "Capacidad mínima") 
            @RequestParam(required = false) Integer minCapacity,
            @Parameter(description = "Capacidad máxima") 
            @RequestParam(required = false) Integer maxCapacity) {
        log.info("Buscando clases por capacidad: {} - {}", minCapacity, maxCapacity);
        
        List<ClassDTO.Response> response = classService.getClassesByCapacity(minCapacity, maxCapacity);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener clases con horarios disponibles",
        description = "Obtiene clases que tienen horarios disponibles para reservas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases con horarios disponibles",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/available-schedules")
    public ResponseEntity<List<ClassDTO.Response>> getClassesWithAvailableSchedules() {
        log.info("Obteniendo clases con horarios disponibles");
        
        List<ClassDTO.Response> response = classService.getClassesWithAvailableSchedules();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Actualizar clase",
        description = "Actualiza la información de una clase existente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Clase actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClassDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Clase no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<ClassDTO.Response> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody ClassDTO.UpdateRequest request) {
        log.info("Actualizando clase: {}", id);
        
        ClassDTO.Response response = classService.updateClass(id, request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desactivar clase",
        description = "Desactiva una clase (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Clase desactivada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Clase no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deactivateClass(@PathVariable Long id) {
        log.info("Desactivando clase: {}", id);
        
        classService.deactivateClass(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Clase desactivada exitosamente",
            "classId", id
        ));
    }

    @Operation(
        summary = "Activar clase",
        description = "Activa una clase previamente desactivada"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Clase activada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Clase no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> activateClass(@PathVariable Long id) {
        log.info("Activando clase: {}", id);
        
        classService.activateClass(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Clase activada exitosamente",
            "classId", id
        ));
    }

    @Operation(
        summary = "Eliminar clase",
        description = "Elimina una clase del sistema (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Clase eliminada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Clase no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteClass(@PathVariable Long id) {
        log.info("Eliminando clase: {}", id);
        
        classService.deleteClass(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Clase eliminada exitosamente",
            "classId", id
        ));
    }

    @Operation(
        summary = "Contar clases activas por sucursal",
        description = "Obtiene el número de clases activas de una sucursal"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conteo de clases activas obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/branch/{branchId}/count")
    public ResponseEntity<Map<String, Object>> countActiveClassesByBranch(@PathVariable Long branchId) {
        log.info("Contando clases activas de la sucursal: {}", branchId);
        
        Long count = classService.countActiveClassesByBranch(branchId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", count,
            "branchId", branchId
        ));
    }

    @Operation(
        summary = "Verificar si clase existe",
        description = "Verifica si una clase existe en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verificación completada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}/exists")
    public ResponseEntity<Map<String, Object>> classExists(@PathVariable Long id) {
        log.info("Verificando si clase existe: {}", id);
        
        boolean exists = classService.classExists(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "exists", exists,
            "classId", id
        ));
    }

    @Operation(
        summary = "Verificar si clase está activa",
        description = "Verifica si una clase está activa"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verificación completada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}/active")
    public ResponseEntity<Map<String, Object>> isClassActive(@PathVariable Long id) {
        log.info("Verificando si clase está activa: {}", id);
        
        boolean active = classService.isClassActive(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "active", active,
            "classId", id
        ));
    }
}
