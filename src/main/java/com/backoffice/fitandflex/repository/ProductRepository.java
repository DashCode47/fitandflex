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
 * Repositorio para gestión de productos
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Buscar productos por sucursal
     */
    List<Product> findByBranchId(Long branchId);
    Page<Product> findByBranchId(Long branchId, Pageable pageable);

    /**
     * Buscar productos por categoría
     */
    List<Product> findByCategory(String category);
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Buscar productos por sucursal y categoría
     */
    List<Product> findByBranchIdAndCategory(Long branchId, String category);
    Page<Product> findByBranchIdAndCategory(Long branchId, String category, Pageable pageable);

    /**
     * Buscar productos por estado activo
     */
    List<Product> findByActiveTrue();
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Buscar productos activos por sucursal
     */
    List<Product> findByBranchIdAndActiveTrue(Long branchId);
    Page<Product> findByBranchIdAndActiveTrue(Long branchId, Pageable pageable);

    /**
     * Buscar productos por marca
     */
    List<Product> findByBrand(String brand);
    Page<Product> findByBrand(String brand, Pageable pageable);

    /**
     * Buscar productos por SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Buscar productos por SKU y sucursal
     */
    Optional<Product> findBySkuAndBranchId(String sku, Long branchId);

    /**
     * Buscar productos por rango de precios
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Buscar productos con stock bajo
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity IS NOT NULL AND p.minStockLevel IS NOT NULL AND p.stockQuantity <= p.minStockLevel")
    List<Product> findLowStockProducts();
    Page<Product> findLowStockProducts(Pageable pageable);

    /**
     * Buscar productos con stock bajo por sucursal
     */
    @Query("SELECT p FROM Product p WHERE p.branch.id = :branchId AND p.stockQuantity IS NOT NULL AND p.minStockLevel IS NOT NULL AND p.stockQuantity <= p.minStockLevel")
    List<Product> findLowStockProductsByBranch(@Param("branchId") Long branchId);
    Page<Product> findLowStockProductsByBranch(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar productos sin stock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity IS NULL OR p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
    Page<Product> findOutOfStockProducts(Pageable pageable);

    /**
     * Buscar productos sin stock por sucursal
     */
    @Query("SELECT p FROM Product p WHERE p.branch.id = :branchId AND (p.stockQuantity IS NULL OR p.stockQuantity = 0)")
    List<Product> findOutOfStockProductsByBranch(@Param("branchId") Long branchId);
    Page<Product> findOutOfStockProductsByBranch(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Buscar productos digitales
     */
    List<Product> findByIsDigitalTrue();
    Page<Product> findByIsDigitalTrue(Pageable pageable);

    /**
     * Buscar productos de suscripción
     */
    List<Product> findByIsSubscriptionTrue();
    Page<Product> findByIsSubscriptionTrue(Pageable pageable);

    /**
     * Buscar productos por nombre (búsqueda parcial)
     */
    List<Product> findByNameContainingIgnoreCase(String name);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Buscar productos por sucursal y nombre
     */
    List<Product> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);
    Page<Product> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name, Pageable pageable);

    /**
     * Buscar productos por descripción (búsqueda parcial)
     */
    List<Product> findByDescriptionContainingIgnoreCase(String description);
    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * Buscar productos que pueden ser vendidos
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND (p.requiresApproval = false OR p.requiresApproval IS NULL) AND (p.isDigital = true OR (p.stockQuantity IS NOT NULL AND p.stockQuantity > 0))")
    List<Product> findAvailableForSale();
    Page<Product> findAvailableForSale(Pageable pageable);

    /**
     * Buscar productos disponibles para venta por sucursal
     */
    @Query("SELECT p FROM Product p WHERE p.branch.id = :branchId AND p.active = true AND (p.requiresApproval = false OR p.requiresApproval IS NULL) AND (p.isDigital = true OR (p.stockQuantity IS NOT NULL AND p.stockQuantity > 0))")
    List<Product> findAvailableForSaleByBranch(@Param("branchId") Long branchId);
    Page<Product> findAvailableForSaleByBranch(@Param("branchId") Long branchId, Pageable pageable);

    /**
     * Contar productos por sucursal
     */
    long countByBranchId(Long branchId);
    long countByBranchIdAndActiveTrue(Long branchId);

    /**
     * Contar productos por categoría
     */
    long countByCategory(String category);
    long countByBranchIdAndCategory(Long branchId, String category);

    /**
     * Contar productos con stock bajo
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity IS NOT NULL AND p.minStockLevel IS NOT NULL AND p.stockQuantity <= p.minStockLevel")
    long countLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.branch.id = :branchId AND p.stockQuantity IS NOT NULL AND p.minStockLevel IS NOT NULL AND p.stockQuantity <= p.minStockLevel")
    long countLowStockProductsByBranch(@Param("branchId") Long branchId);

    /**
     * Contar productos sin stock
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity IS NULL OR p.stockQuantity = 0")
    long countOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.branch.id = :branchId AND (p.stockQuantity IS NULL OR p.stockQuantity = 0)")
    long countOutOfStockProductsByBranch(@Param("branchId") Long branchId);

    /**
     * Sumar valor total del inventario por sucursal
     */
    @Query("SELECT SUM(p.price * p.stockQuantity) FROM Product p WHERE p.branch.id = :branchId AND p.stockQuantity IS NOT NULL")
    BigDecimal sumInventoryValueByBranch(@Param("branchId") Long branchId);

    /**
     * Buscar productos por múltiples criterios
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:branchId IS NULL OR p.branch.id = :branchId) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:isDigital IS NULL OR p.isDigital = :isDigital) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> findByMultipleCriteria(
            @Param("branchId") Long branchId,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("active") Boolean active,
            @Param("isDigital") Boolean isDigital,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Product p WHERE " +
           "(:branchId IS NULL OR p.branch.id = :branchId) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:isDigital IS NULL OR p.isDigital = :isDigital) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findByMultipleCriteria(
            @Param("branchId") Long branchId,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("active") Boolean active,
            @Param("isDigital") Boolean isDigital,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}