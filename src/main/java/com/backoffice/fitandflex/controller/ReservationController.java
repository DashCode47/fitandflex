package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.dto.ReservationDTO;
import com.backoffice.fitandflex.entity.ReservationStatus;
import com.backoffice.fitandflex.service.ReservationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de reservas
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservations", description = "Endpoints para gestión de reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;
    
    /**
     * Helper method para crear Pageable
     */
    private Pageable createPageable(int page, int size, String sort) {
        return org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
    }

    /**
     * Crear nueva reserva
     */
    @Operation(
        summary = "Crear nueva reserva",
        description = "Crea una nueva reserva para un usuario en un horario específico."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Reserva creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o conflicto de reserva",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ReservationDTO.Response>> createReservation(
            @Valid @RequestBody ReservationDTO.CreateRequest request) {
        
        log.info("Creando nueva reserva para usuario {} en horario {}", request.getUserId(), request.getScheduleId());
        ReservationDTO.Response reservation = reservationService.createReservation(request);
        log.info("Reserva creada exitosamente con ID: {}", reservation.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.SuccessResponse.<ReservationDTO.Response>builder()
                        .success(true)
                        .message("Reserva creada exitosamente")
                        .data(reservation)
                        .build());
    }

    /**
     * Obtener todas las reservas (paginado)
     */
    @Operation(
        summary = "Obtener todas las reservas",
        description = "Retorna una lista paginada de todas las reservas del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de reservas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere autenticación"
        )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ReservationDTO.Response>> getAllReservations(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ReservationDTO.Response> reservations = reservationService.getAllReservations(pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Obtener todas las reservas (sin autenticación para pruebas)
     */
    @GetMapping("/test")
    public ResponseEntity<Page<ReservationDTO.Response>> getAllReservationsTest(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ReservationDTO.Response> reservations = reservationService.getAllReservations(pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Obtener resumen de todas las reservas
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<List<ReservationDTO.SummaryResponse>>> getAllReservationsSummary() {
        List<ReservationDTO.SummaryResponse> reservations = reservationService.getAllReservationsSummary();
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<List<ReservationDTO.SummaryResponse>>builder()
                .success(true)
                .message("Reservas obtenidas exitosamente")
                .data(reservations)
                .build());
    }

    /**
     * Obtener reserva por ID
     */
    @Operation(
        summary = "Obtener reserva por ID",
        description = "Retorna los detalles de una reserva específica por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Reserva encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Reserva no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ReservationDTO.Response>> getReservationById(
            @Parameter(description = "ID de la reserva") @PathVariable Long id) {
        ReservationDTO.Response reservation = reservationService.getReservationById(id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ReservationDTO.Response>builder()
                .success(true)
                .message("Reserva obtenida exitosamente")
                .data(reservation)
                .build());
    }

    /**
     * Obtener reservas por usuario
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ReservationDTO.Response>> getReservationsByUser(
            @PathVariable Long userId,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ReservationDTO.Response> reservations = reservationService.getReservationsByUser(userId, pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Obtener reservas por horario
     */
    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ReservationDTO.Response>> getReservationsBySchedule(
            @PathVariable Long scheduleId,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ReservationDTO.Response> reservations = reservationService.getReservationsBySchedule(scheduleId, pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Obtener reservas por estado
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ReservationDTO.Response>> getReservationsByStatus(
            @PathVariable ReservationStatus status,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ReservationDTO.Response> reservations = reservationService.getReservationsByStatus(status, pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Obtener reservas por sucursal
     */
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ReservationDTO.Response>> getReservationsByBranch(
            @PathVariable Long branchId,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ReservationDTO.Response> reservations = reservationService.getReservationsByBranch(branchId, pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Obtener reservas por clase
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ReservationDTO.Response>> getReservationsByClass(
            @PathVariable Long classId,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        Pageable pageable = createPageable(page, size, sort);
        
        Page<ReservationDTO.Response> reservations = reservationService.getReservationsByClass(classId, pageable);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Obtener reservas futuras por usuario
     */
    @GetMapping("/user/{userId}/future")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<List<ReservationDTO.Response>>> getFutureReservationsByUser(
            @PathVariable Long userId) {
        List<ReservationDTO.Response> reservations = reservationService.getFutureReservationsByUser(userId);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<List<ReservationDTO.Response>>builder()
                .success(true)
                .message("Reservas futuras obtenidas exitosamente")
                .data(reservations)
                .build());
    }

    /**
     * Obtener reservas pasadas por usuario
     */
    @GetMapping("/user/{userId}/past")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<List<ReservationDTO.Response>>> getPastReservationsByUser(
            @PathVariable Long userId) {
        List<ReservationDTO.Response> reservations = reservationService.getPastReservationsByUser(userId);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<List<ReservationDTO.Response>>builder()
                .success(true)
                .message("Reservas pasadas obtenidas exitosamente")
                .data(reservations)
                .build());
    }

    /**
     * Actualizar reserva
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<ReservationDTO.Response>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationDTO.UpdateRequest request) {
        
        ReservationDTO.Response reservation = reservationService.updateReservation(id, request);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ReservationDTO.Response>builder()
                .success(true)
                .message("Reserva actualizada exitosamente")
                .data(reservation)
                .build());
    }

    /**
     * Cancelar reserva
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ReservationDTO.Response>> cancelReservation(
            @PathVariable Long id) {
        
        log.info("Cancelando reserva con ID: {}", id);
        ReservationDTO.Response reservation = reservationService.cancelReservation(id);
        log.info("Reserva cancelada exitosamente con ID: {}", reservation.getId());
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ReservationDTO.Response>builder()
                .success(true)
                .message("Reserva cancelada exitosamente")
                .data(reservation)
                .build());
    }

    /**
     * Marcar asistencia
     */
    @PostMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<ReservationDTO.Response>> markAttendance(
            @PathVariable Long id) {
        
        log.info("Marcando asistencia para reserva con ID: {}", id);
        ReservationDTO.Response reservation = reservationService.markAttendance(id);
        log.info("Asistencia marcada exitosamente para reserva con ID: {}", reservation.getId());
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ReservationDTO.Response>builder()
                .success(true)
                .message("Asistencia marcada exitosamente")
                .data(reservation)
                .build());
    }

    /**
     * Marcar no asistencia
     */
    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<ReservationDTO.Response>> markNoShow(
            @PathVariable Long id) {
        
        log.info("Marcando no asistencia para reserva con ID: {}", id);
        ReservationDTO.Response reservation = reservationService.markNoShow(id);
        log.info("No asistencia marcada exitosamente para reserva con ID: {}", reservation.getId());
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ReservationDTO.Response>builder()
                .success(true)
                .message("No asistencia marcada exitosamente")
                .data(reservation)
                .build());
    }

    /**
     * Eliminar reserva
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Void>> deleteReservation(
            @PathVariable Long id) {
        
        log.info("Eliminando reserva con ID: {}", id);
        reservationService.deleteReservation(id);
        log.info("Reserva eliminada exitosamente con ID: {}", id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<Void>builder()
                .success(true)
                .message("Reserva eliminada exitosamente")
                .build());
    }

    /**
     * Verificar si existe una reserva
     */
    @GetMapping("/exists")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<Boolean>> checkReservationExists(
            @RequestParam Long userId,
            @RequestParam Long scheduleId) {
        
        boolean exists = reservationService.existsReservation(userId, scheduleId);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<Boolean>builder()
                .success(true)
                .message("Verificación completada")
                .data(exists)
                .build());
    }

    /**
     * Obtener estadísticas de reservas
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getReservationStats() {
        CommonDto.SuccessResponse<Object> stats = reservationService.getReservationStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener estadísticas de reservas por usuario
     */
    @GetMapping("/stats/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getReservationStatsByUser(
            @PathVariable Long userId) {
        CommonDto.SuccessResponse<Object> stats = reservationService.getReservationStatsByUser(userId);
        return ResponseEntity.ok(stats);
    }
}
