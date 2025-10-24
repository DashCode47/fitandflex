package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.UserMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de membresías de usuario
 */
@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    /**
     * Buscar membresías por usuario
     */
    List<UserMembership> findByUserId(Long userId);
    Page<UserMembership> findByUserId(Long userId, Pageable pageable);

    /**
     * Buscar membresías por producto
     */
    List<UserMembership> findByProductId(Long productId);
    Page<UserMembership> findByProductId(Long productId, Pageable pageable);

    /**
     * Buscar membresías por estado
     */
    List<UserMembership> findByStatus(String status);
    Page<UserMembership> findByStatus(String status, Pageable pageable);

    /**
     * Buscar membresías activas
     */
    List<UserMembership> findByActiveTrue();
    Page<UserMembership> findByActiveTrue(Pageable pageable);

    /**
     * Buscar membresías inactivas
     */
    List<UserMembership> findByActiveFalse();
    Page<UserMembership> findByActiveFalse(Pageable pageable);

    /**
     * Buscar membresías activas por usuario
     */
    List<UserMembership> findByUserIdAndActiveTrue(Long userId);
    Page<UserMembership> findByUserIdAndActiveTrue(Long userId, Pageable pageable);

    /**
     * Buscar membresías por usuario y estado
     */
    List<UserMembership> findByUserIdAndStatus(Long userId, String status);
    Page<UserMembership> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    /**
     * Buscar membresías por sucursal
     */
    @Query("SELECT um FROM UserMembership um WHERE um.product.branch.id = :branchId")
    List<UserMembership> findByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT um FROM UserMembership um WHERE um.product.branch.id = :branchId")
    Page<UserMembership> findByBranchId(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar membresías activas por sucursal
     */
    @Query("SELECT um FROM UserMembership um WHERE um.product.branch.id = :branchId AND um.active = true")
    List<UserMembership> findActiveByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT um FROM UserMembership um WHERE um.product.branch.id = :branchId AND um.active = true")
    Page<UserMembership> findActiveByBranchId(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar membresías que expiran en un rango de fechas
     */
    @Query("SELECT um FROM UserMembership um WHERE um.endDate BETWEEN :startDate AND :endDate AND um.active = true")
    List<UserMembership> findExpiringBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar membresías vencidas
     */
    @Query("SELECT um FROM UserMembership um WHERE um.endDate < :now AND um.active = true")
    List<UserMembership> findExpired(@Param("now") LocalDateTime now);

    /**
     * Buscar membresías que expiran pronto (próximos N días)
     */
    @Query("SELECT um FROM UserMembership um WHERE um.endDate BETWEEN :now AND :futureDate AND um.active = true")
    List<UserMembership> findExpiringSoon(@Param("now") LocalDateTime now, 
                                         @Param("futureDate") LocalDateTime futureDate);

    /**
     * Buscar membresías activas por usuario
     */
    @Query("SELECT um FROM UserMembership um WHERE um.user.id = :userId AND um.active = true AND um.status = 'ACTIVE' AND um.endDate > :now")
    List<UserMembership> findActiveMembershipsByUser(@Param("userId") Long userId, 
                                                     @Param("now") LocalDateTime now);

    /**
     * Verificar si usuario tiene membresía activa
     */
    @Query("SELECT COUNT(um) > 0 FROM UserMembership um WHERE um.user.id = :userId AND um.active = true AND um.status = 'ACTIVE' AND um.endDate > :now")
    boolean hasActiveMembership(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Buscar membresías por tipo de producto
     */
    @Query("SELECT um FROM UserMembership um WHERE um.product.membershipType = :membershipType")
    List<UserMembership> findByMembershipType(@Param("membershipType") String membershipType);
    
    @Query("SELECT um FROM UserMembership um WHERE um.product.membershipType = :membershipType")
    Page<UserMembership> findByMembershipType(@Param("membershipType") String membershipType, Pageable pageable);

    /**
     * Buscar membresías por categoría de producto
     */
    @Query("SELECT um FROM UserMembership um WHERE um.product.category = :category")
    List<UserMembership> findByProductCategory(@Param("category") String category);
    
    @Query("SELECT um FROM UserMembership um WHERE um.product.category = :category")
    Page<UserMembership> findByProductCategory(@Param("category") String category, Pageable pageable);

    /**
     * Contar membresías activas por usuario
     */
    @Query("SELECT COUNT(um) FROM UserMembership um WHERE um.user.id = :userId AND um.active = true")
    Long countActiveMembershipsByUser(@Param("userId") Long userId);

    /**
     * Contar membresías activas por sucursal
     */
    @Query("SELECT COUNT(um) FROM UserMembership um WHERE um.product.branch.id = :branchId AND um.active = true")
    Long countActiveMembershipsByBranch(@Param("branchId") Long branchId);

    /**
     * Buscar membresías por usuario y producto
     */
    Optional<UserMembership> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Buscar membresías activas por usuario y producto
     */
    @Query("SELECT um FROM UserMembership um WHERE um.user.id = :userId AND um.product.id = :productId AND um.active = true AND um.status = 'ACTIVE'")
    Optional<UserMembership> findActiveByUserIdAndProductId(@Param("userId") Long userId, 
                                                           @Param("productId") Long productId);

    /**
     * Buscar membresías que expiran en los próximos días
     */
    @Query("SELECT um FROM UserMembership um WHERE um.endDate BETWEEN :now AND :expirationDate AND um.active = true ORDER BY um.endDate ASC")
    List<UserMembership> findExpiringInDays(@Param("now") LocalDateTime now, 
                                          @Param("expirationDate") LocalDateTime expirationDate);

    /**
     * Buscar membresías creadas en un rango de fechas
     */
    @Query("SELECT um FROM UserMembership um WHERE um.createdAt BETWEEN :startDate AND :endDate")
    List<UserMembership> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar membresías por quien las asignó
     */
    List<UserMembership> findByAssignedById(Long assignedById);
    Page<UserMembership> findByAssignedById(Long assignedById, Pageable pageable);
}
