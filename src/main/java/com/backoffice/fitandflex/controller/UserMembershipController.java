package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.UserMembershipDTO;
import com.backoffice.fitandflex.service.UserMembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de membresías de usuario
 */
@RestController
@RequestMapping("/api/user-memberships")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Memberships", description = "Endpoints para gestión de membresías de usuario")
@SecurityRequirement(name = "bearerAuth")
public class UserMembershipController {

    private final UserMembershipService userMembershipService;

    /**
     * Asignar membresía a usuario
     */
    @Operation(
        summary = "Asignar membresía a usuario",
        description = "Asigna una membresía (producto) a un usuario con fechas específicas de inicio y fin."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Membresía asignada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserMembershipDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o conflicto de membresía",
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
    public ResponseEntity<UserMembershipDTO.Response> assignMembership(
            @Valid @RequestBody UserMembershipDTO.CreateRequest request,
            Authentication authentication) {
        
        log.info("Asignando membresía {} al usuario {}", request.getProductId(), request.getUserId());
        
        String assignedByEmail = authentication.getName();
        UserMembershipDTO.Response response = userMembershipService.assignMembership(request, assignedByEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener todas las membresías (paginado)
     */
    @Operation(
        summary = "Obtener todas las membresías",
        description = "Retorna una lista paginada de todas las membresías del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol SUPER_ADMIN o BRANCH_ADMIN"
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Page<UserMembershipDTO.Response>> getAllMemberships(
            @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        
        log.info("Obteniendo todas las membresías con paginación: {}", pageable);
        Page<UserMembershipDTO.Response> response = userMembershipService.getAllMemberships(pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener membresía por ID
     */
    @Operation(
        summary = "Obtener membresía por ID",
        description = "Retorna los detalles de una membresía específica por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Membresía encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserMembershipDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Membresía no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserMembershipDTO.Response> getMembershipById(
            @Parameter(description = "ID de la membresía") @PathVariable Long id) {
        
        log.info("Obteniendo membresía con ID: {}", id);
        UserMembershipDTO.Response response = userMembershipService.getMembershipById(id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener membresías por usuario
     */
    @Operation(
        summary = "Obtener membresías por usuario",
        description = "Retorna todas las membresías asignadas a un usuario específico."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías del usuario obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#userId, authentication.name)")
    public ResponseEntity<List<UserMembershipDTO.Response>> getMembershipsByUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        
        log.info("Obteniendo membresías del usuario: {}", userId);
        List<UserMembershipDTO.Response> response = userMembershipService.getMembershipsByUser(userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener membresías activas por usuario
     */
    @Operation(
        summary = "Obtener membresías activas por usuario",
        description = "Retorna solo las membresías activas de un usuario específico."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías activas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#userId, authentication.name)")
    public ResponseEntity<List<UserMembershipDTO.Response>> getActiveMembershipsByUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        
        log.info("Obteniendo membresías activas del usuario: {}", userId);
        List<UserMembershipDTO.Response> response = userMembershipService.getActiveMembershipsByUser(userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener membresías por sucursal
     */
    @Operation(
        summary = "Obtener membresías por sucursal",
        description = "Retorna todas las membresías de una sucursal específica."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías de la sucursal obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<UserMembershipDTO.Response>> getMembershipsByBranch(
            @Parameter(description = "ID de la sucursal") @PathVariable Long branchId) {
        
        log.info("Obteniendo membresías de la sucursal: {}", branchId);
        List<UserMembershipDTO.Response> response = userMembershipService.getMembershipsByBranch(branchId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener membresías activas por sucursal
     */
    @Operation(
        summary = "Obtener membresías activas por sucursal",
        description = "Retorna solo las membresías activas de una sucursal específica."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías activas de la sucursal obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/branch/{branchId}/active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<UserMembershipDTO.Response>> getActiveMembershipsByBranch(
            @Parameter(description = "ID de la sucursal") @PathVariable Long branchId) {
        
        log.info("Obteniendo membresías activas de la sucursal: {}", branchId);
        List<UserMembershipDTO.Response> response = userMembershipService.getActiveMembershipsByBranch(branchId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener membresías que expiran pronto
     */
    @Operation(
        summary = "Obtener membresías que expiran pronto",
        description = "Retorna las membresías que expiran en los próximos N días."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías que expiran pronto obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<UserMembershipDTO.Response>> getExpiringMemberships(
            @Parameter(description = "Número de días para considerar 'pronto'", required = false)
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("Obteniendo membresías que expiran en los próximos {} días", days);
        List<UserMembershipDTO.Response> response = userMembershipService.getExpiringMemberships(days);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener membresías vencidas
     */
    @Operation(
        summary = "Obtener membresías vencidas",
        description = "Retorna todas las membresías que han vencido."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías vencidas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/expired")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<UserMembershipDTO.Response>> getExpiredMemberships() {
        
        log.info("Obteniendo membresías vencidas");
        List<UserMembershipDTO.Response> response = userMembershipService.getExpiredMemberships();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar membresía
     */
    @Operation(
        summary = "Actualizar membresía",
        description = "Actualiza la información de una membresía existente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Membresía actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserMembershipDTO.Response.class)
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
            description = "Membresía no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserMembershipDTO.Response> updateMembership(
            @Parameter(description = "ID de la membresía") @PathVariable Long id,
            @Valid @RequestBody UserMembershipDTO.UpdateRequest request) {
        
        log.info("Actualizando membresía: {}", id);
        UserMembershipDTO.Response response = userMembershipService.updateMembership(id, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cambiar estado de membresía
     */
    @Operation(
        summary = "Cambiar estado de membresía",
        description = "Cambia el estado de una membresía (ACTIVE, CANCELLED, SUSPENDED, EXPIRED)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estado de membresía cambiado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserMembershipDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Estado inválido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Membresía no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserMembershipDTO.Response> changeMembershipStatus(
            @Parameter(description = "ID de la membresía") @PathVariable Long id,
            @Valid @RequestBody UserMembershipDTO.ChangeStatusRequest request) {
        
        log.info("Cambiando estado de membresía {} a {}", id, request.getStatus());
        UserMembershipDTO.Response response = userMembershipService.changeMembershipStatus(id, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Extender membresía
     */
    @Operation(
        summary = "Extender membresía",
        description = "Extiende la duración de una membresía por un número específico de días."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Membresía extendida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserMembershipDTO.Response.class)
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
            description = "Membresía no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/extend")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserMembershipDTO.Response> extendMembership(
            @Parameter(description = "ID de la membresía") @PathVariable Long id,
            @Valid @RequestBody UserMembershipDTO.ExtendRequest request) {
        
        log.info("Extendiendo membresía {} por {} días", id, request.getAdditionalDays());
        UserMembershipDTO.Response response = userMembershipService.extendMembership(id, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar membresía
     */
    @Operation(
        summary = "Eliminar membresía",
        description = "Elimina una membresía del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Membresía eliminada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Membresía no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteMembership(
            @Parameter(description = "ID de la membresía") @PathVariable Long id) {
        
        log.info("Eliminando membresía: {}", id);
        userMembershipService.deleteMembership(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Membresía eliminada exitosamente",
            "membershipId", id
        ));
    }

    /**
     * Verificar si usuario tiene membresía activa
     */
    @Operation(
        summary = "Verificar membresía activa",
        description = "Verifica si un usuario tiene al menos una membresía activa."
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
    @GetMapping("/user/{userId}/has-active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#userId, authentication.name)")
    public ResponseEntity<Map<String, Object>> hasActiveMembership(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        
        log.info("Verificando si usuario {} tiene membresía activa", userId);
        boolean hasActive = userMembershipService.hasActiveMembership(userId);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "hasActiveMembership", hasActive
        ));
    }

    /**
     * Obtener resumen de membresías por usuario
     */
    @Operation(
        summary = "Obtener resumen de membresías por usuario",
        description = "Retorna un resumen de todas las membresías de un usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resumen de membresías obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#userId, authentication.name)")
    public ResponseEntity<List<UserMembershipDTO.SummaryResponse>> getMembershipSummaryByUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        
        log.info("Obteniendo resumen de membresías del usuario: {}", userId);
        List<UserMembershipDTO.SummaryResponse> response = userMembershipService.getMembershipSummaryByUser(userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Registrar abono adicional a una membresía
     */
    @Operation(
        summary = "Registrar abono adicional",
        description = "Registra un pago adicional para una membresía, reduciendo el saldo pendiente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Abono registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserMembershipDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o membresía ya pagada completamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Membresía no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserMembershipDTO.Response> addPaymentToMembership(
            @Parameter(description = "ID de la membresía") @PathVariable Long id,
            @Valid @RequestBody UserMembershipDTO.AddPaymentRequest request) {
        
        log.info("Registrando abono de {} para membresía {}", request.getAmount(), id);
        UserMembershipDTO.Response response = userMembershipService.addPaymentToMembership(id, request);
        
        return ResponseEntity.ok(response);
    }
}
