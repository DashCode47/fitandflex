package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.ClassDTO;
import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.service.ClassService;
import com.backoffice.fitandflex.service.ClassSubscriptionService;
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

import java.time.LocalDate;
import java.time.LocalTime;
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
    private final ClassSubscriptionService subscriptionService;
    
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
        summary = "Obtener clases activas por fecha específica",
        description = "Obtiene todas las clases activas con sus horarios para una fecha específica. " +
                      "Expande los patrones recurrentes en instancias específicas para esa fecha."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases activas para la fecha obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/active/date/{date}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<List<ClassDTO.ResponseWithDate>> getActiveClassesByDate(
            @Parameter(description = "Fecha específica para consultar clases (formato yyyy-MM-dd)", example = "2025-11-18", required = true)
            @PathVariable java.time.LocalDate date) {
        log.info("Obteniendo clases activas para la fecha: {}", date);
        
        List<ClassDTO.ResponseWithDate> response = classService.getActiveClassesByDate(date);
        
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

    @Operation(
        summary = "Asignar día de la semana desde fecha del calendario",
        description = "Asigna un día de la semana a una clase desde una fecha seleccionada del calendario. " +
                      "Si es recurrente, crea un patrón que se repetirá cada semana en ese día. " +
                      "Si no es recurrente, crea un horario específico para esa fecha."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Día asignado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClassDTO.Response.class)
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
            description = "Clase no encontrada",
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
    @PostMapping("/{id}/assign-day")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<ClassDTO.Response> assignDayFromDate(
            @PathVariable Long id,
            @Valid @RequestBody ClassDTO.AssignDayFromDateRequest request) {
        log.info("Asignando día desde fecha para clase {}: fecha={}, recurrente={}", 
                id, request.getDate(), request.getRecurrent());
        
        ClassDTO.Response response = classService.assignDayFromDate(id, request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Crear suscripción de usuario a clase",
        description = "Permite que un usuario se suscriba/reserve a una clase con un horario específico o recurrente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Suscripción creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o conflicto de suscripción",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Clase o usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping("/{id}/subscribe")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ClassDTO.SubscriptionResponse>> createSubscription(
            @PathVariable Long id,
            @Valid @RequestBody ClassDTO.CreateSubscriptionRequest request) {
        log.info("Creando suscripción para usuario {} en clase {}", request.getUserId(), id);
        
        ClassDTO.SubscriptionResponse subscription = subscriptionService.createSubscription(id, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.SuccessResponse.<ClassDTO.SubscriptionResponse>builder()
                        .success(true)
                        .message("Suscripción creada exitosamente")
                        .data(subscription)
                        .build());
    }

    @Operation(
        summary = "Obtener suscripciones de una clase",
        description = "Obtiene todas las suscripciones activas de una clase específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de suscripciones obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/{id}/subscriptions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<List<ClassDTO.SubscriptionResponse>> getSubscriptionsByClassId(@PathVariable Long id) {
        log.info("Obteniendo suscripciones de la clase: {}", id);
        
        List<ClassDTO.SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByClassId(id);
        
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(
        summary = "Obtener usuarios de una clase",
        description = "Obtiene todos los usuarios únicos suscritos a una clase"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<List<com.backoffice.fitandflex.dto.UserDTO.SummaryResponse>> getUsersByClassId(@PathVariable Long id) {
        log.info("Obteniendo usuarios de la clase: {}", id);
        
        List<com.backoffice.fitandflex.dto.UserDTO.SummaryResponse> users = subscriptionService.getUsersByClassId(id);
        
        return ResponseEntity.ok(users);
    }

    @Operation(
        summary = "Obtener usuarios de un horario específico de una clase",
        description = "Obtiene todos los usuarios únicos suscritos a un horario específico de una clase. " +
                      "Incluye tanto suscripciones recurrentes como específicas que coinciden con el rango de horas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/{id}/users/time")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<List<com.backoffice.fitandflex.dto.UserDTO.SummaryResponse>> getUsersByClassAndTimeRange(
            @PathVariable Long id,
            @Parameter(description = "Hora de inicio del rango (formato HH:mm:ss)", example = "18:00:00", required = true)
            @RequestParam LocalTime startTime,
            @Parameter(description = "Hora de fin del rango (formato HH:mm:ss)", example = "19:30:00", required = true)
            @RequestParam LocalTime endTime,
            @Parameter(description = "Fecha específica (opcional). Si se proporciona, solo retorna usuarios suscritos para esa fecha específica", example = "2025-11-18")
            @RequestParam(required = false) LocalDate date) {
        
        List<com.backoffice.fitandflex.dto.UserDTO.SummaryResponse> users;
        
        if (date != null) {
            // Si se proporciona fecha, buscar usuarios para esa fecha específica
            log.info("Obteniendo usuarios de la clase {} para fecha {} y horario {} - {}", id, date, startTime, endTime);
            users = subscriptionService.getUsersByClassTimeAndDate(id, date, startTime, endTime);
        } else {
            // Si no se proporciona fecha, buscar usuarios para el horario (incluye recurrentes y específicas)
            log.info("Obteniendo usuarios de la clase {} para horario {} - {}", id, startTime, endTime);
            users = subscriptionService.getUsersByClassAndTimeRange(id, startTime, endTime);
        }
        
        return ResponseEntity.ok(users);
    }

    @Operation(
        summary = "Obtener clases de un usuario",
        description = "Obtiene todas las clases a las que un usuario está suscrito"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<List<ClassDTO.SummaryResponse>> getClassesByUserId(@PathVariable Long userId) {
        log.info("Obteniendo clases del usuario: {}", userId);
        
        List<ClassDTO.SummaryResponse> classes = subscriptionService.getClassesByUserId(userId);
        
        return ResponseEntity.ok(classes);
    }

    @Operation(
        summary = "Obtener suscripciones de un usuario",
        description = "Obtiene todas las suscripciones activas de un usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de suscripciones obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/user/{userId}/subscriptions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<List<ClassDTO.SubscriptionResponse>> getSubscriptionsByUserId(@PathVariable Long userId) {
        log.info("Obteniendo suscripciones del usuario: {}", userId);
        
        List<ClassDTO.SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(
        summary = "Cancelar suscripción",
        description = "Cancela una suscripción marcándola como inactiva"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Suscripción cancelada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Suscripción no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/subscriptions/{subscriptionId}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ClassDTO.SubscriptionResponse>> cancelSubscription(
            @PathVariable Long subscriptionId) {
        log.info("Cancelando suscripción: {}", subscriptionId);
        
        ClassDTO.SubscriptionResponse subscription = subscriptionService.cancelSubscription(subscriptionId);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ClassDTO.SubscriptionResponse>builder()
                .success(true)
                .message("Suscripción cancelada exitosamente")
                .data(subscription)
                .build());
    }

    @Operation(
        summary = "Eliminar suscripción",
        description = "Elimina permanentemente una suscripción del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Suscripción eliminada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Suscripción no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @DeleteMapping("/subscriptions/{subscriptionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSubscription(@PathVariable Long subscriptionId) {
        log.info("Eliminando suscripción: {}", subscriptionId);
        
        subscriptionService.deleteSubscription(subscriptionId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Suscripción eliminada exitosamente",
            "subscriptionId", subscriptionId
        ));
    }

    @Operation(
        summary = "Cancelar suscripción por usuario, clase y fecha",
        description = "Cancela una suscripción activa identificándola por usuario, clase y fecha específica. " +
                      "Marca la suscripción como inactiva. Si hay múltiples suscripciones para la misma fecha, " +
                      "cancela la primera encontrada. Para cancelar una específica, usa startTime y endTime."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Suscripción cancelada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Suscripción no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{classId}/users/{userId}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ClassDTO.SubscriptionResponse>> cancelSubscriptionByUserClassAndDate(
            @Parameter(description = "ID de la clase", example = "7", required = true)
            @PathVariable Long classId,
            @Parameter(description = "ID del usuario", example = "3", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Fecha específica de la suscripción (formato yyyy-MM-dd)", example = "2025-11-19", required = true)
            @RequestParam java.time.LocalDate date,
            @Parameter(description = "Hora de inicio (opcional). Si se proporciona junto con endTime, cancela solo esa suscripción específica", example = "12:00:00")
            @RequestParam(required = false) java.time.LocalTime startTime,
            @Parameter(description = "Hora de fin (opcional). Debe proporcionarse junto con startTime", example = "13:30:00")
            @RequestParam(required = false) java.time.LocalTime endTime) {
        log.info("Cancelando suscripción para usuario {}, clase {} y fecha {}", userId, classId, date);
        
        ClassDTO.SubscriptionResponse subscription;
        
        // Si se proporcionan startTime y endTime, cancelar la suscripción específica
        if (startTime != null && endTime != null) {
            subscription = subscriptionService.cancelSubscriptionByUserClassDateAndTime(
                    userId, classId, date, startTime, endTime);
        } else {
            // Si no, cancelar la primera suscripción encontrada para esa fecha
            subscription = subscriptionService.cancelSubscriptionByUserClassAndDate(
                    userId, classId, date);
        }
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ClassDTO.SubscriptionResponse>builder()
                .success(true)
                .message("Suscripción cancelada exitosamente")
                .data(subscription)
                .build());
    }
}
