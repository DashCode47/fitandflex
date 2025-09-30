package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de pagos
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Buscar pagos por usuario
     */
    List<Payment> findByUserId(Long userId);
    Page<Payment> findByUserId(Long userId, Pageable pageable);

    /**
     * Buscar pagos por reserva
     */
    List<Payment> findByReservationId(Long reservationId);
    Page<Payment> findByReservationId(Long reservationId, Pageable pageable);

    /**
     * Buscar pagos por estado
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    /**
     * Buscar pagos por método de pago
     */
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    Page<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod, Pageable pageable);

    /**
     * Buscar pagos por usuario y estado
     */
    List<Payment> findByUserIdAndStatus(Long userId, Payment.PaymentStatus status);
    Page<Payment> findByUserIdAndStatus(Long userId, Payment.PaymentStatus status, Pageable pageable);

    /**
     * Buscar pagos por usuario y método de pago
     */
    List<Payment> findByUserIdAndPaymentMethod(Long userId, Payment.PaymentMethod paymentMethod);
    Page<Payment> findByUserIdAndPaymentMethod(Long userId, Payment.PaymentMethod paymentMethod, Pageable pageable);

    /**
     * Buscar pagos por rango de fechas
     */
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Buscar pagos por usuario y rango de fechas
     */
    List<Payment> findByUserIdAndPaymentDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    Page<Payment> findByUserIdAndPaymentDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Buscar pagos por rango de montos
     */
    List<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    Page<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Buscar pagos por usuario y rango de montos
     */
    List<Payment> findByUserIdAndAmountBetween(Long userId, BigDecimal minAmount, BigDecimal maxAmount);
    Page<Payment> findByUserIdAndAmountBetween(Long userId, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Buscar pagos por ID de transacción
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Buscar pagos por referencia del gateway
     */
    List<Payment> findByGatewayReference(String gatewayReference);
    Page<Payment> findByGatewayReference(String gatewayReference, Pageable pageable);

    /**
     * Buscar pagos con reembolsos
     */
    List<Payment> findByRefundAmountIsNotNull();
    Page<Payment> findByRefundAmountIsNotNull(Pageable pageable);

    /**
     * Buscar pagos por usuario con reembolsos
     */
    List<Payment> findByUserIdAndRefundAmountIsNotNull(Long userId);
    Page<Payment> findByUserIdAndRefundAmountIsNotNull(Long userId, Pageable pageable);

    /**
     * Buscar pagos por sucursal
     */
    @Query("SELECT p FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId")
    List<Payment> findByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT p FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId")
    Page<Payment> findByBranchId(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar pagos por sucursal y estado
     */
    @Query("SELECT p FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId AND p.status = :status")
    List<Payment> findByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId AND p.status = :status")
    Page<Payment> findByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") Payment.PaymentStatus status, Pageable pageable);

    /**
     * Contar pagos por usuario
     */
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, Payment.PaymentStatus status);
    long countByUserIdAndPaymentMethod(Long userId, Payment.PaymentMethod paymentMethod);

    /**
     * Contar pagos por estado y método
     */
    long countByStatus(Payment.PaymentStatus status);
    long countByPaymentMethod(Payment.PaymentMethod paymentMethod);

    /**
     * Contar pagos por sucursal
     */
    @Query("SELECT COUNT(p) FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId")
    long countByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(p) FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId AND p.status = :status")
    long countByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") Payment.PaymentStatus status);

    /**
     * Sumar montos por usuario
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user.id = :userId AND p.status = :status")
    BigDecimal sumAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Payment.PaymentStatus status);

    /**
     * Sumar montos por sucursal
     */
    @Query("SELECT SUM(p.amount) FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId")
    BigDecimal sumAmountByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT SUM(p.amount) FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId AND p.status = :status")
    BigDecimal sumAmountByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") Payment.PaymentStatus status);

    /**
     * Sumar reembolsos
     */
    @Query("SELECT SUM(p.refundAmount) FROM Payment p WHERE p.user.id = :userId AND p.refundAmount IS NOT NULL")
    BigDecimal sumRefundAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(p.refundAmount) FROM Payment p JOIN p.user u WHERE u.branch.id = :branchId AND p.refundAmount IS NOT NULL")
    BigDecimal sumRefundAmountByBranchId(@Param("branchId") Long branchId);

    /**
     * Búsqueda por descripción
     */
    List<Payment> findByDescriptionContainingIgnoreCase(String description);
    Page<Payment> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    List<Payment> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String description);
    Page<Payment> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String description, Pageable pageable);
}