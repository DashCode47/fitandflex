package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de membresías
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Buscar membresías por sucursal
     */
    List<Product> findByBranchId(Long branchId);
    Page<Product> findByBranchId(Long branchId, Pageable pageable);

    /**
     * Buscar membresías por categoría
     */
    List<Product> findByCategory(String category);
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Buscar membresías por sucursal y categoría
     */
    List<Product> findByBranchIdAndCategory(Long branchId, String category);
    Page<Product> findByBranchIdAndCategory(Long branchId, String category, Pageable pageable);

    /**
     * Buscar membresías por estado activo
     */
    List<Product> findByActiveTrue();
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Buscar membresías activas por sucursal
     */
    List<Product> findByBranchIdAndActiveTrue(Long branchId);
    Page<Product> findByBranchIdAndActiveTrue(Long branchId, Pageable pageable);

    /**
     * Buscar membresías por tipo
     */
    List<Product> findByMembershipType(String membershipType);
    Page<Product> findByMembershipType(String membershipType, Pageable pageable);

    /**
     * Buscar membresías por SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Buscar membresías por SKU y sucursal
     */
    Optional<Product> findBySkuAndBranchId(String sku, Long branchId);

    /**
     * Buscar membresías por rango de precios
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Buscar membresías por duración
     */
    List<Product> findByDurationDays(Integer durationDays);
    Page<Product> findByDurationDays(Integer durationDays, Pageable pageable);

    /**
     * Buscar membresías por rango de duración
     */
    List<Product> findByDurationDaysBetween(Integer minDays, Integer maxDays);
    Page<Product> findByDurationDaysBetween(Integer minDays, Integer maxDays, Pageable pageable);

    /**
     * Buscar membresías con período de prueba
     */
    List<Product> findByTrialPeriodDaysGreaterThan(Integer minTrialDays);
    Page<Product> findByTrialPeriodDaysGreaterThan(Integer minTrialDays, Pageable pageable);

    /**
     * Buscar membresías con renovación automática
     */
    List<Product> findByAutoRenewalTrue();
    Page<Product> findByAutoRenewalTrue(Pageable pageable);

    /**
     * Buscar membresías sin renovación automática
     */
    List<Product> findByAutoRenewalFalse();
    Page<Product> findByAutoRenewalFalse(Pageable pageable);

    /**
     * Buscar membresías por nombre (búsqueda parcial)
     */
    List<Product> findByNameContainingIgnoreCase(String name);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Buscar membresías por descripción (búsqueda parcial)
     */
    List<Product> findByDescriptionContainingIgnoreCase(String description);
    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * Buscar membresías por beneficios (búsqueda parcial)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.benefits) LIKE LOWER(CONCAT('%', :benefit, '%'))")
    List<Product> findByBenefitsContainingIgnoreCase(@Param("benefit") String benefit);

    @Query("SELECT p FROM Product p WHERE LOWER(p.benefits) LIKE LOWER(CONCAT('%', :benefit, '%'))")
    Page<Product> findByBenefitsContainingIgnoreCase(@Param("benefit") String benefit, Pageable pageable);

    /**
     * Buscar membresías por características (búsqueda parcial)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.features) LIKE LOWER(CONCAT('%', :feature, '%'))")
    List<Product> findByFeaturesContainingIgnoreCase(@Param("feature") String feature);

    @Query("SELECT p FROM Product p WHERE LOWER(p.features) LIKE LOWER(CONCAT('%', :feature, '%'))")
    Page<Product> findByFeaturesContainingIgnoreCase(@Param("feature") String feature, Pageable pageable);

    /**
     * Buscar membresías disponibles
     */
    @Query("SELECT p FROM Product p WHERE p.active = true")
    List<Product> findAvailableMemberships();

    @Query("SELECT p FROM Product p WHERE p.active = true")
    Page<Product> findAvailableMemberships(Pageable pageable);

    /**
     * Buscar membresías disponibles por sucursal
     */
    @Query("SELECT p FROM Product p WHERE p.branch.id = :branchId AND p.active = true")
    List<Product> findAvailableMembershipsByBranch(@Param("branchId") Long branchId);

    @Query("SELECT p FROM Product p WHERE p.branch.id = :branchId AND p.active = true")
    Page<Product> findAvailableMembershipsByBranch(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar membresías por máximo de usuarios
     */
    List<Product> findByMaxUsers(Integer maxUsers);
    Page<Product> findByMaxUsers(Integer maxUsers, Pageable pageable);

    /**
     * Buscar membresías ilimitadas (sin límite de usuarios)
     */
    @Query("SELECT p FROM Product p WHERE p.maxUsers IS NULL OR p.maxUsers <= 0")
    List<Product> findUnlimitedMemberships();

    @Query("SELECT p FROM Product p WHERE p.maxUsers IS NULL OR p.maxUsers <= 0")
    Page<Product> findUnlimitedMemberships(Pageable pageable);

    /**
     * Contar membresías por sucursal
     */
    long countByBranchId(Long branchId);
    long countByActiveTrue();
    long countByBranchIdAndActiveTrue(Long branchId);

    /**
     * Contar membresías por categoría
     */
    long countByCategory(String category);
    long countByBranchIdAndCategory(Long branchId, String category);

    /**
     * Contar membresías por tipo
     */
    long countByMembershipType(String membershipType);
    long countByBranchIdAndMembershipType(Long branchId, String membershipType);

    /**
     * Contar membresías con renovación automática
     */
    long countByAutoRenewalTrue();
    long countByBranchIdAndAutoRenewalTrue(Long branchId);

    /**
     * Contar membresías con período de prueba
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.trialPeriodDays IS NOT NULL AND p.trialPeriodDays > 0")
    long countMembershipsWithTrialPeriod();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.branch.id = :branchId AND p.trialPeriodDays IS NOT NULL AND p.trialPeriodDays > 0")
    long countMembershipsWithTrialPeriodByBranch(@Param("branchId") Long branchId);

    /**
     * Sumar valor total de membresías por sucursal
     */
    @Query("SELECT SUM(p.price) FROM Product p WHERE p.branch.id = :branchId")
    BigDecimal sumPriceByBranch(@Param("branchId") Long branchId);

    /**
     * Buscar membresías por múltiples criterios
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:branchId IS NULL OR p.branch.id = :branchId) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:membershipType IS NULL OR p.membershipType = :membershipType) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:autoRenewal IS NULL OR p.autoRenewal = :autoRenewal) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minDuration IS NULL OR p.durationDays >= :minDuration) AND " +
           "(:maxDuration IS NULL OR p.durationDays <= :maxDuration)")
    List<Product> findByMultipleCriteria(
            @Param("branchId") Long branchId,
            @Param("category") String category,
            @Param("membershipType") String membershipType,
            @Param("active") Boolean active,
            @Param("autoRenewal") Boolean autoRenewal,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration);

    @Query("SELECT p FROM Product p WHERE " +
           "(:branchId IS NULL OR p.branch.id = :branchId) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:membershipType IS NULL OR p.membershipType = :membershipType) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:autoRenewal IS NULL OR p.autoRenewal = :autoRenewal) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minDuration IS NULL OR p.durationDays >= :minDuration) AND " +
           "(:maxDuration IS NULL OR p.durationDays <= :maxDuration)")
    Page<Product> findByMultipleCriteria(
            @Param("branchId") Long branchId,
            @Param("category") String category,
            @Param("membershipType") String membershipType,
            @Param("active") Boolean active,
            @Param("autoRenewal") Boolean autoRenewal,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            Pageable pageable);
}