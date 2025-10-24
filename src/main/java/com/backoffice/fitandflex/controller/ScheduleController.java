package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.ScheduleDTO;
import com.backoffice.fitandflex.service.ScheduleService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de horarios
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedules", description = "Endpoints para gestión de horarios")
public class ScheduleController {

    private final ScheduleService scheduleService;
    
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
        summary = "Crear nuevo horario",
        description = "Crea un nuevo horario para una clase. Solo usuarios con rol SUPER_ADMIN o BRANCH_ADMIN pueden realizar esta operación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Horario creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o conflicto de horarios",
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
    public ResponseEntity<ScheduleDTO.Response> createSchedule(@Valid @RequestBody ScheduleDTO.CreateRequest request) {
        log.info("Creando nuevo horario para clase: {}", request.getClassId());
        
        ScheduleDTO.Response response = scheduleService.createSchedule(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Obtener horario por ID",
        description = "Obtiene la información completa de un horario por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Horario encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Horario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO.Response> getScheduleById(@PathVariable Long id) {
        log.info("Obteniendo horario por ID: {}", id);
        
        ScheduleDTO.Response response = scheduleService.getScheduleById(id);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener todos los horarios",
        description = "Obtiene una lista paginada de todos los horarios del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<ScheduleDTO.Response>> getAllSchedules(
            @Parameter(description = "Número de página (por defecto: 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10, máximo: 100)") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: 'startTime')") 
            @RequestParam(defaultValue = "startTime") String sort,
            @Parameter(description = "Dirección del ordenamiento (asc/desc, por defecto: 'asc')") 
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Obteniendo todos los horarios con paginación - page: {}, size: {}, sort: {}, direction: {}", 
                page, size, sort, direction);
        
        org.springframework.data.domain.Pageable pageable = createPageable(page, size, sort, direction);
        
        Page<ScheduleDTO.Response> response = scheduleService.getAllSchedules(pageable);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios por clase",
        description = "Obtiene todos los horarios de una clase específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios de la clase obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ScheduleDTO.Response>> getSchedulesByClass(@PathVariable Long classId) {
        log.info("Obteniendo horarios de la clase: {}", classId);
        
        List<ScheduleDTO.Response> response = scheduleService.getSchedulesByClass(classId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios activos por clase",
        description = "Obtiene todos los horarios activos de una clase específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios activos de la clase obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/class/{classId}/active")
    public ResponseEntity<List<ScheduleDTO.Response>> getActiveSchedulesByClass(@PathVariable Long classId) {
        log.info("Obteniendo horarios activos de la clase: {}", classId);
        
        List<ScheduleDTO.Response> response = scheduleService.getActiveSchedulesByClass(classId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios activos",
        description = "Obtiene todos los horarios activos del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios activos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/active")
    public ResponseEntity<List<ScheduleDTO.Response>> getActiveSchedules() {
        log.info("Obteniendo horarios activos");
        
        List<ScheduleDTO.Response> response = scheduleService.getActiveSchedules();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios futuros",
        description = "Obtiene todos los horarios futuros del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios futuros obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/future")
    public ResponseEntity<List<ScheduleDTO.Response>> getFutureSchedules() {
        log.info("Obteniendo horarios futuros");
        
        List<ScheduleDTO.Response> response = scheduleService.getFutureSchedules();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios disponibles",
        description = "Obtiene horarios que tienen cupos disponibles para reservas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios disponibles obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/available")
    public ResponseEntity<List<ScheduleDTO.Response>> getAvailableSchedules() {
        log.info("Obteniendo horarios disponibles");
        
        List<ScheduleDTO.Response> response = scheduleService.getAvailableSchedules();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios por sucursal",
        description = "Obtiene todos los horarios de una sucursal específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios de la sucursal obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ScheduleDTO.Response>> getSchedulesByBranch(@PathVariable Long branchId) {
        log.info("Obteniendo horarios de la sucursal: {}", branchId);
        
        List<ScheduleDTO.Response> response = scheduleService.getSchedulesByBranch(branchId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios por rango de fechas",
        description = "Obtiene horarios dentro de un rango de fechas específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios en el rango de fechas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<ScheduleDTO.Response>> getSchedulesByDateRange(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Obteniendo horarios por rango de fechas: {} - {}", startDate, endDate);
        
        List<ScheduleDTO.Response> response = scheduleService.getSchedulesByDateRange(startDate, endDate);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios por fecha específica",
        description = "Obtiene horarios de una fecha específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios de la fecha obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/date")
    public ResponseEntity<List<ScheduleDTO.Response>> getSchedulesByDate(
            @Parameter(description = "Fecha específica (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        log.info("Obteniendo horarios por fecha: {}", date);
        
        List<ScheduleDTO.Response> response = scheduleService.getSchedulesByDate(date);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios próximos",
        description = "Obtiene horarios de los próximos 7 días"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios próximos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduleDTO.Response>> getUpcomingSchedules() {
        log.info("Obteniendo horarios próximos");
        
        List<ScheduleDTO.Response> response = scheduleService.getUpcomingSchedules();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener horarios por día de la semana",
        description = "Obtiene horarios de un día específico de la semana (1=Lunes, 7=Domingo)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de horarios del día obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/day-of-week/{dayOfWeek}")
    public ResponseEntity<List<ScheduleDTO.Response>> getSchedulesByDayOfWeek(@PathVariable Integer dayOfWeek) {
        log.info("Obteniendo horarios por día de la semana: {}", dayOfWeek);
        
        List<ScheduleDTO.Response> response = scheduleService.getSchedulesByDayOfWeek(dayOfWeek);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Actualizar horario",
        description = "Actualiza la información de un horario existente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Horario actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o conflicto de horarios",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Horario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<ScheduleDTO.Response> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleDTO.UpdateRequest request) {
        log.info("Actualizando horario: {}", id);
        
        ScheduleDTO.Response response = scheduleService.updateSchedule(id, request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desactivar horario",
        description = "Desactiva un horario (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Horario desactivado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Horario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deactivateSchedule(@PathVariable Long id) {
        log.info("Desactivando horario: {}", id);
        
        scheduleService.deactivateSchedule(id);
        log.info("Horario desactivado exitosamente: {}", id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Horario desactivado exitosamente",
            "scheduleId", id
        ));
    }

    @Operation(
        summary = "Activar horario",
        description = "Activa un horario previamente desactivado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Horario activado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Horario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> activateSchedule(@PathVariable Long id) {
        log.info("Activando horario: {}", id);
        
        scheduleService.activateSchedule(id);
        log.info("Horario activado exitosamente: {}", id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Horario activado exitosamente",
            "scheduleId", id
        ));
    }

    @Operation(
        summary = "Eliminar horario",
        description = "Elimina un horario del sistema (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Horario eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Horario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long id) {
        log.info("Eliminando horario: {}", id);
        
        scheduleService.deleteSchedule(id);
        log.info("Horario eliminado exitosamente: {}", id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Horario eliminado exitosamente",
            "scheduleId", id
        ));
    }

    @Operation(
        summary = "Contar horarios activos por clase",
        description = "Obtiene el número de horarios activos de una clase"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conteo de horarios activos obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/class/{classId}/count")
    public ResponseEntity<Map<String, Long>> countActiveSchedulesByClass(@PathVariable Long classId) {
        log.info("Contando horarios activos de la clase: {}", classId);
        
        Long count = scheduleService.countActiveSchedulesByClass(classId);
        
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(
        summary = "Verificar si horario existe",
        description = "Verifica si un horario existe en el sistema"
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
    public ResponseEntity<Map<String, Boolean>> scheduleExists(@PathVariable Long id) {
        log.info("Verificando si horario existe: {}", id);
        
        boolean exists = scheduleService.scheduleExists(id);
        
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(
        summary = "Verificar si horario está activo",
        description = "Verifica si un horario está activo"
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
    public ResponseEntity<Map<String, Boolean>> isScheduleActive(@PathVariable Long id) {
        log.info("Verificando si horario está activo: {}", id);
        
        boolean active = scheduleService.isScheduleActive(id);
        
        return ResponseEntity.ok(Map.of("active", active));
    }

    @Operation(
        summary = "Verificar si horario tiene cupos disponibles",
        description = "Verifica si un horario tiene cupos disponibles para reservas"
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
    @GetMapping("/{id}/available-spots")
    public ResponseEntity<Map<String, Object>> getAvailableSpots(@PathVariable Long id) {
        log.info("Obteniendo cupos disponibles del horario: {}", id);
        
        boolean hasSpots = scheduleService.hasAvailableSpots(id);
        Integer availableSpots = scheduleService.getAvailableSpots(id);
        
        return ResponseEntity.ok(Map.of(
            "hasAvailableSpots", hasSpots,
            "availableSpots", availableSpots
        ));
    }
}
