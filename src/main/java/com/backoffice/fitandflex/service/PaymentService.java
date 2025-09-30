package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.dto.PaymentDTO;
import com.backoffice.fitandflex.entity.Payment;
import com.backoffice.fitandflex.entity.Reservation;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.repository.PaymentRepository;
import com.backoffice.fitandflex.repository.ReservationRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de pagos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Crear un nuevo pago
     */
    public PaymentDTO.Response createPayment(PaymentDTO.CreateRequest request) {
        log.info("Creando pago para usuario {} con monto {}", request.getUserId(), request.getAmount());

        // Validar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + request.getUserId()));

        // Validar que la reserva existe (si se proporciona)
        Reservation reservation = null;
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + request.getReservationId()));
        }

        // Crear el pago
        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .user(user)
                .reservation(reservation)
                .transactionId(request.getTransactionId())
                .gatewayReference(request.getGatewayReference())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Pago creado exitosamente con ID: {}", savedPayment.getId());

        return PaymentDTO.fromEntity(savedPayment);
    }

    /**
     * Obtener todos los pagos con paginación
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getAllPayments(Pageable pageable) {
        log.info("Obteniendo todos los pagos con paginación: {}", pageable);
        
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener resumen de todos los pagos
     */
    @Transactional(readOnly = true)
    public List<PaymentDTO.SummaryResponse> getAllPaymentsSummary() {
        log.info("Obteniendo resumen de todos los pagos");
        
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(PaymentDTO::fromEntityToSummary)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pago por ID
     */
    @Transactional(readOnly = true)
    public PaymentDTO.Response getPaymentById(Long id) {
        log.info("Obteniendo pago con ID: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));
        
        return PaymentDTO.fromEntity(payment);
    }

    /**
     * Obtener pagos por usuario
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByUser(Long userId, Pageable pageable) {
        log.info("Obteniendo pagos para usuario {} con paginación: {}", userId, pageable);
        
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }
        
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener pagos por reserva
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByReservation(Long reservationId, Pageable pageable) {
        log.info("Obteniendo pagos para reserva {} con paginación: {}", reservationId, pageable);
        
        // Validar que la reserva existe
        if (!reservationRepository.existsById(reservationId)) {
            throw new IllegalArgumentException("Reserva no encontrada con ID: " + reservationId);
        }
        
        Page<Payment> payments = paymentRepository.findByReservationId(reservationId, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener pagos por estado
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable) {
        log.info("Obteniendo pagos con estado {} con paginación: {}", status, pageable);
        
        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener pagos por método de pago
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable) {
        log.info("Obteniendo pagos con método {} con paginación: {}", paymentMethod, pageable);
        
        Page<Payment> payments = paymentRepository.findByPaymentMethod(paymentMethod, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener pagos por sucursal
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo pagos para sucursal {} con paginación: {}", branchId, pageable);
        
        Page<Payment> payments = paymentRepository.findByBranchId(branchId, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener pagos por rango de fechas
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Obteniendo pagos entre {} y {} con paginación: {}", startDate, endDate, pageable);
        
        Page<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener pagos por rango de montos
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        log.info("Obteniendo pagos entre ${} y ${} con paginación: {}", minAmount, maxAmount, pageable);
        
        Page<Payment> payments = paymentRepository.findByAmountBetween(minAmount, maxAmount, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Obtener pagos con reembolsos
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> getPaymentsWithRefunds(Pageable pageable) {
        log.info("Obteniendo pagos con reembolsos con paginación: {}", pageable);
        
        Page<Payment> payments = paymentRepository.findByRefundAmountIsNotNull(pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Buscar pagos por descripción
     */
    @Transactional(readOnly = true)
    public Page<PaymentDTO.Response> searchPaymentsByDescription(String description, Pageable pageable) {
        log.info("Buscando pagos por descripción '{}' con paginación: {}", description, pageable);
        
        Page<Payment> payments = paymentRepository.findByDescriptionContainingIgnoreCase(description, pageable);
        return payments.map(PaymentDTO::fromEntity);
    }

    /**
     * Actualizar pago
     */
    public PaymentDTO.Response updatePayment(Long id, PaymentDTO.UpdateRequest request) {
        log.info("Actualizando pago con ID: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));
        
        // Actualizar campos si se proporcionan
        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            payment.setDescription(request.getDescription());
        }
        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }
        if (request.getGatewayReference() != null) {
            payment.setGatewayReference(request.getGatewayReference());
        }
        if (request.getGatewayResponse() != null) {
            payment.setGatewayResponse(request.getGatewayResponse());
        }
        if (request.getFailureReason() != null) {
            payment.setFailureReason(request.getFailureReason());
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Pago actualizado exitosamente con ID: {}", updatedPayment.getId());
        
        return PaymentDTO.fromEntity(updatedPayment);
    }

    /**
     * Marcar pago como completado
     */
    public PaymentDTO.Response markPaymentAsCompleted(Long id, String transactionId, String gatewayReference) {
        log.info("Marcando pago {} como completado", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));
        
        payment.markAsCompleted(transactionId, gatewayReference);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Pago marcado como completado exitosamente con ID: {}", updatedPayment.getId());
        
        return PaymentDTO.fromEntity(updatedPayment);
    }

    /**
     * Marcar pago como fallido
     */
    public PaymentDTO.Response markPaymentAsFailed(Long id, String failureReason) {
        log.info("Marcando pago {} como fallido", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));
        
        payment.markAsFailed(failureReason);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Pago marcado como fallido exitosamente con ID: {}", updatedPayment.getId());
        
        return PaymentDTO.fromEntity(updatedPayment);
    }

    /**
     * Procesar reembolso
     */
    public PaymentDTO.Response processRefund(Long id, PaymentDTO.RefundRequest request) {
        log.info("Procesando reembolso de ${} para pago {}", request.getRefundAmount(), id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));
        
        // Validar que el pago puede ser reembolsado
        if (!payment.canBeRefunded()) {
            throw new IllegalArgumentException("Este pago no puede ser reembolsado");
        }
        
        // Validar que el monto del reembolso no exceda el monto del pago
        if (request.getRefundAmount().compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("El monto del reembolso no puede exceder el monto del pago");
        }
        
        payment.processRefund(request.getRefundAmount(), request.getRefundReason());
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Reembolso procesado exitosamente para pago con ID: {}", updatedPayment.getId());
        
        return PaymentDTO.fromEntity(updatedPayment);
    }

    /**
     * Eliminar pago
     */
    public void deletePayment(Long id) {
        log.info("Eliminando pago con ID: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));
        
        // Validar que el pago puede ser eliminado
        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED && payment.getRefundAmount() == null) {
            throw new IllegalArgumentException("No se puede eliminar un pago completado sin reembolso");
        }
        
        paymentRepository.delete(payment);
        log.info("Pago eliminado exitosamente con ID: {}", id);
    }

    /**
     * Obtener estadísticas de pagos
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getPaymentStats() {
        long totalPayments = paymentRepository.count();
        long pendingPayments = paymentRepository.countByStatus(Payment.PaymentStatus.PENDING);
        long completedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.COMPLETED);
        long failedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.FAILED);
        long cancelledPayments = paymentRepository.countByStatus(Payment.PaymentStatus.CANCELLED);
        long refundedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.REFUNDED);
        long partiallyRefundedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(java.util.Map.of(
                    "totalPayments", totalPayments,
                    "pendingPayments", pendingPayments,
                    "completedPayments", completedPayments,
                    "failedPayments", failedPayments,
                    "cancelledPayments", cancelledPayments,
                    "refundedPayments", refundedPayments,
                    "partiallyRefundedPayments", partiallyRefundedPayments
                ))
                .build();
    }

    /**
     * Obtener estadísticas de pagos por usuario
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getPaymentStatsByUser(Long userId) {
        // Validar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }
        
        long totalPayments = paymentRepository.countByUserId(userId);
        long completedPayments = paymentRepository.countByUserIdAndStatus(userId, Payment.PaymentStatus.COMPLETED);
        BigDecimal totalAmount = paymentRepository.sumAmountByUserId(userId);
        BigDecimal totalRefunded = paymentRepository.sumRefundAmountByUserId(userId);
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas del usuario obtenidas exitosamente")
                .data(java.util.Map.of(
                    "userId", userId,
                    "totalPayments", totalPayments,
                    "completedPayments", completedPayments,
                    "totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO,
                    "totalRefunded", totalRefunded != null ? totalRefunded : BigDecimal.ZERO
                ))
                .build();
    }

    /**
     * Obtener estadísticas de pagos por sucursal
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getPaymentStatsByBranch(Long branchId) {
        long totalPayments = paymentRepository.countByBranchId(branchId);
        long completedPayments = paymentRepository.countByBranchIdAndStatus(branchId, Payment.PaymentStatus.COMPLETED);
        BigDecimal totalAmount = paymentRepository.sumAmountByBranchId(branchId);
        BigDecimal totalRefunded = paymentRepository.sumRefundAmountByBranchId(branchId);
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas de la sucursal obtenidas exitosamente")
                .data(java.util.Map.of(
                    "branchId", branchId,
                    "totalPayments", totalPayments,
                    "completedPayments", completedPayments,
                    "totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO,
                    "totalRefunded", totalRefunded != null ? totalRefunded : BigDecimal.ZERO
                ))
                .build();
    }
}
