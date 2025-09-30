package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.dto.PaymentDTO;
import com.backoffice.fitandflex.entity.Payment;
import com.backoffice.fitandflex.service.PaymentService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de pagos
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Endpoints para gestión de pagos")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Crear nuevo pago
     */
    @Operation(
        summary = "Crear nuevo pago",
        description = "Crea un nuevo pago en el sistema. Solo registra pagos externos ya realizados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Pago creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
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
            description = "Acceso denegado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<PaymentDTO.Response>> createPayment(
            @Valid @RequestBody PaymentDTO.CreateRequest request) {
        
        PaymentDTO.Response payment = paymentService.createPayment(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.SuccessResponse.<PaymentDTO.Response>builder()
                        .success(true)
                        .message("Pago creado exitosamente")
                        .data(payment)
                        .build());
    }

    /**
     * Obtener todos los pagos (paginado)
     */
    @Operation(
        summary = "Obtener todos los pagos",
        description = "Retorna una lista paginada de todos los pagos del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de pagos obtenida exitosamente",
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
    public ResponseEntity<Page<PaymentDTO.Response>> getAllPayments(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener todos los pagos (sin autenticación para pruebas)
     */
    @GetMapping("/test")
    public ResponseEntity<Page<PaymentDTO.Response>> getAllPaymentsTest(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener resumen de todos los pagos
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<List<PaymentDTO.SummaryResponse>>> getAllPaymentsSummary() {
        List<PaymentDTO.SummaryResponse> payments = paymentService.getAllPaymentsSummary();
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<List<PaymentDTO.SummaryResponse>>builder()
                .success(true)
                .message("Pagos obtenidos exitosamente")
                .data(payments)
                .build());
    }

    /**
     * Obtener pago por ID
     */
    @Operation(
        summary = "Obtener pago por ID",
        description = "Retorna los detalles de un pago específico por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pago encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<PaymentDTO.Response>> getPaymentById(
            @Parameter(description = "ID del pago") @PathVariable Long id) {
        PaymentDTO.Response payment = paymentService.getPaymentById(id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<PaymentDTO.Response>builder()
                .success(true)
                .message("Pago obtenido exitosamente")
                .data(payment)
                .build());
    }

    /**
     * Obtener pagos por usuario
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByUser(
            @PathVariable Long userId,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByUser(userId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener pagos por reserva
     */
    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByReservation(
            @PathVariable Long reservationId,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByReservation(reservationId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener pagos por estado
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByStatus(
            @PathVariable Payment.PaymentStatus status,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener pagos por método de pago
     */
    @GetMapping("/method/{paymentMethod}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByPaymentMethod(
            @PathVariable Payment.PaymentMethod paymentMethod,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByPaymentMethod(paymentMethod, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener pagos por sucursal
     */
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByBranch(
            @PathVariable Long branchId,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByBranch(branchId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener pagos por rango de fechas
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener pagos por rango de montos
     */
    @GetMapping("/amount-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsByAmountRange(minAmount, maxAmount, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Obtener pagos con reembolsos
     */
    @GetMapping("/refunds")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> getPaymentsWithRefunds(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.getPaymentsWithRefunds(pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Buscar pagos por descripción
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<PaymentDTO.Response>> searchPaymentsByDescription(
            @RequestParam String description,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<PaymentDTO.Response> payments = paymentService.searchPaymentsByDescription(description, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Actualizar pago
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<PaymentDTO.Response>> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO.UpdateRequest request) {
        
        PaymentDTO.Response payment = paymentService.updatePayment(id, request);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<PaymentDTO.Response>builder()
                .success(true)
                .message("Pago actualizado exitosamente")
                .data(payment)
                .build());
    }

    /**
     * Marcar pago como completado
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<PaymentDTO.Response>> markPaymentAsCompleted(
            @PathVariable Long id,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String gatewayReference) {
        
        PaymentDTO.Response payment = paymentService.markPaymentAsCompleted(id, transactionId, gatewayReference);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<PaymentDTO.Response>builder()
                .success(true)
                .message("Pago marcado como completado exitosamente")
                .data(payment)
                .build());
    }

    /**
     * Marcar pago como fallido
     */
    @PostMapping("/{id}/fail")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<PaymentDTO.Response>> markPaymentAsFailed(
            @PathVariable Long id,
            @RequestParam String failureReason) {
        
        PaymentDTO.Response payment = paymentService.markPaymentAsFailed(id, failureReason);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<PaymentDTO.Response>builder()
                .success(true)
                .message("Pago marcado como fallido exitosamente")
                .data(payment)
                .build());
    }

    /**
     * Procesar reembolso
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<PaymentDTO.Response>> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO.RefundRequest request) {
        
        PaymentDTO.Response payment = paymentService.processRefund(id, request);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<PaymentDTO.Response>builder()
                .success(true)
                .message("Reembolso procesado exitosamente")
                .data(payment)
                .build());
    }

    /**
     * Eliminar pago
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Void>> deletePayment(
            @PathVariable Long id) {
        
        paymentService.deletePayment(id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<Void>builder()
                .success(true)
                .message("Pago eliminado exitosamente")
                .build());
    }

    /**
     * Obtener estadísticas de pagos
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getPaymentStats() {
        CommonDto.SuccessResponse<Object> stats = paymentService.getPaymentStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener estadísticas de pagos por usuario
     */
    @GetMapping("/stats/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getPaymentStatsByUser(
            @PathVariable Long userId) {
        CommonDto.SuccessResponse<Object> stats = paymentService.getPaymentStatsByUser(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener estadísticas de pagos por sucursal
     */
    @GetMapping("/stats/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getPaymentStatsByBranch(
            @PathVariable Long branchId) {
        CommonDto.SuccessResponse<Object> stats = paymentService.getPaymentStatsByBranch(branchId);
        return ResponseEntity.ok(stats);
    }
}
