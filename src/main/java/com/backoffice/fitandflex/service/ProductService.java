package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.dto.ProductDTO;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.entity.Product;
import com.backoffice.fitandflex.repository.BranchRepository;
import com.backoffice.fitandflex.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de membresías
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    /**
     * Crear una nueva membresía
     */
    public ProductDTO.Response createProduct(ProductDTO.CreateRequest request) {
        log.info("Creando membresía '{}' para sucursal {}", request.getName(), request.getBranchId());

        // Validar que la sucursal existe
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + request.getBranchId()));

        // Validar que el SKU no existe en la sucursal
        if (request.getSku() != null && productRepository.findBySkuAndBranchId(request.getSku(), request.getBranchId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una membresía con SKU '" + request.getSku() + "' en esta sucursal");
        }

        // Crear la membresía
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .sku(request.getSku())
                .membershipType(request.getMembershipType())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .maxUsers(request.getMaxUsers())
                .active(request.getActive())
                .autoRenewal(request.getAutoRenewal())
                .trialPeriodDays(request.getTrialPeriodDays())
                .imageUrl(request.getImageUrl())
                .benefits(request.getBenefits())
                .features(request.getFeatures())
                .branch(branch)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Membresía creada exitosamente con ID: {}", savedProduct.getId());

        return ProductDTO.fromEntity(savedProduct);
    }

    /**
     * Obtener todas las membresías con paginación
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getAllProducts(Pageable pageable) {
        log.info("Obteniendo todas las membresías con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener resumen de todas las membresías
     */
    @Transactional(readOnly = true)
    public List<ProductDTO.SummaryResponse> getAllProductsSummary() {
        log.info("Obteniendo resumen de todas las membresías");
        
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductDTO::fromEntityToSummary)
                .collect(Collectors.toList());
    }

    /**
     * Obtener membresía por ID
     */
    @Transactional(readOnly = true)
    public ProductDTO.Response getProductById(Long id) {
        log.info("Obteniendo membresía con ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada con ID: " + id));
        
        return ProductDTO.fromEntity(product);
    }

    /**
     * Obtener membresías por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo membresías para sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findByBranchId(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías por categoría
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByCategory(String category, Pageable pageable) {
        log.info("Obteniendo membresías de categoría '{}' con paginación: {}", category, pageable);
        
        Page<Product> products = productRepository.findByCategory(category, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías por sucursal y categoría
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByBranchAndCategory(Long branchId, String category, Pageable pageable) {
        log.info("Obteniendo membresías de sucursal {} y categoría '{}' con paginación: {}", branchId, category, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findByBranchIdAndCategory(branchId, category, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías activas
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getActiveProducts(Pageable pageable) {
        log.info("Obteniendo membresías activas con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías activas por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getActiveProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo membresías activas de sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findByBranchIdAndActiveTrue(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías por tipo
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByMembershipType(String membershipType, Pageable pageable) {
        log.info("Obteniendo membresías de tipo '{}' con paginación: {}", membershipType, pageable);
        
        Page<Product> products = productRepository.findByMembershipType(membershipType, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías por rango de precios
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.info("Obteniendo membresías entre ${} y ${} con paginación: {}", minPrice, maxPrice, pageable);
        
        Page<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías por duración
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByDuration(Integer durationDays, Pageable pageable) {
        log.info("Obteniendo membresías con duración {} días con paginación: {}", durationDays, pageable);
        
        Page<Product> products = productRepository.findByDurationDays(durationDays, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías por rango de duración
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByDurationRange(Integer minDays, Integer maxDays, Pageable pageable) {
        log.info("Obteniendo membresías entre {} y {} días con paginación: {}", minDays, maxDays, pageable);
        
        Page<Product> products = productRepository.findByDurationDaysBetween(minDays, maxDays, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías con período de prueba
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsWithTrialPeriod(Integer minTrialDays, Pageable pageable) {
        log.info("Obteniendo membresías con período de prueba mínimo {} días con paginación: {}", minTrialDays, pageable);
        
        Page<Product> products = productRepository.findByTrialPeriodDaysGreaterThan(minTrialDays, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías con renovación automática
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsWithAutoRenewal(Pageable pageable) {
        log.info("Obteniendo membresías con renovación automática con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findByAutoRenewalTrue(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías sin renovación automática
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsWithoutAutoRenewal(Pageable pageable) {
        log.info("Obteniendo membresías sin renovación automática con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findByAutoRenewalFalse(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Buscar membresías por nombre
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProductsByName(String name, Pageable pageable) {
        log.info("Buscando membresías por nombre '{}' con paginación: {}", name, pageable);
        
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Buscar membresías por descripción
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProductsByDescription(String description, Pageable pageable) {
        log.info("Buscando membresías por descripción '{}' con paginación: {}", description, pageable);
        
        Page<Product> products = productRepository.findByDescriptionContainingIgnoreCase(description, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Buscar membresías por beneficios
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProductsByBenefits(String benefit, Pageable pageable) {
        log.info("Buscando membresías por beneficio '{}' con paginación: {}", benefit, pageable);
        
        Page<Product> products = productRepository.findByBenefitsContainingIgnoreCase(benefit, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Buscar membresías por características
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProductsByFeatures(String feature, Pageable pageable) {
        log.info("Buscando membresías por característica '{}' con paginación: {}", feature, pageable);
        
        Page<Product> products = productRepository.findByFeaturesContainingIgnoreCase(feature, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías disponibles
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getAvailableProducts(Pageable pageable) {
        log.info("Obteniendo membresías disponibles con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findAvailableMemberships(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías disponibles por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getAvailableProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo membresías disponibles de sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findAvailableMembershipsByBranch(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener membresías ilimitadas
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getUnlimitedProducts(Pageable pageable) {
        log.info("Obteniendo membresías ilimitadas con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findUnlimitedMemberships(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Buscar membresías por múltiples criterios
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProductsByCriteria(
            Long branchId, String category, String membershipType, Boolean active, 
            Boolean autoRenewal, BigDecimal minPrice, BigDecimal maxPrice, 
            Integer minDuration, Integer maxDuration, Pageable pageable) {
        
        log.info("Buscando membresías con criterios: branchId={}, category={}, membershipType={}, active={}, autoRenewal={}, minPrice={}, maxPrice={}, minDuration={}, maxDuration={}", 
                branchId, category, membershipType, active, autoRenewal, minPrice, maxPrice, minDuration, maxDuration);
        
        Page<Product> products = productRepository.findByMultipleCriteria(
                branchId, category, membershipType, active, autoRenewal, 
                minPrice, maxPrice, minDuration, maxDuration, pageable);
        
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Actualizar membresía
     */
    public ProductDTO.Response updateProduct(Long id, ProductDTO.UpdateRequest request) {
        log.info("Actualizando membresía con ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada con ID: " + id));
        
        // Actualizar campos si se proporcionan
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getSku() != null) {
            // Validar que el SKU no existe en otra sucursal
            if (productRepository.findBySkuAndBranchId(request.getSku(), product.getBranch().getId()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una membresía con SKU '" + request.getSku() + "' en esta sucursal");
            }
            product.setSku(request.getSku());
        }
        if (request.getMembershipType() != null) {
            product.setMembershipType(request.getMembershipType());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDurationDays() != null) {
            product.setDurationDays(request.getDurationDays());
        }
        if (request.getMaxUsers() != null) {
            product.setMaxUsers(request.getMaxUsers());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getAutoRenewal() != null) {
            product.setAutoRenewal(request.getAutoRenewal());
        }
        if (request.getTrialPeriodDays() != null) {
            product.setTrialPeriodDays(request.getTrialPeriodDays());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getBenefits() != null) {
            product.setBenefits(request.getBenefits());
        }
        if (request.getFeatures() != null) {
            product.setFeatures(request.getFeatures());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Membresía actualizada exitosamente con ID: {}", updatedProduct.getId());
        
        return ProductDTO.fromEntity(updatedProduct);
    }

    /**
     * Eliminar membresía
     */
    public void deleteProduct(Long id) {
        log.info("Eliminando membresía con ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada con ID: " + id));
        
        // Validar que la membresía puede ser eliminada
        if (product.getActive()) {
            throw new IllegalArgumentException("No se puede eliminar una membresía activa. Desactívela primero.");
        }
        
        productRepository.delete(product);
        log.info("Membresía eliminada exitosamente con ID: {}", id);
    }

    /**
     * Obtener estadísticas de membresías
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getProductStats() {
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByActiveTrue();
        long autoRenewalProducts = productRepository.countByAutoRenewalTrue();
        long trialProducts = productRepository.countMembershipsWithTrialPeriod();
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(Map.of(
                    "totalMemberships", totalProducts,
                    "activeMemberships", activeProducts,
                    "autoRenewalMemberships", autoRenewalProducts,
                    "trialMemberships", trialProducts
                ))
                .build();
    }

    /**
     * Obtener estadísticas de membresías por sucursal
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getProductStatsByBranch(Long branchId) {
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        long totalProducts = productRepository.countByBranchId(branchId);
        long activeProducts = productRepository.countByBranchIdAndActiveTrue(branchId);
        long autoRenewalProducts = productRepository.countByBranchIdAndAutoRenewalTrue(branchId);
        long trialProducts = productRepository.countMembershipsWithTrialPeriodByBranch(branchId);
        BigDecimal totalValue = productRepository.sumPriceByBranch(branchId);
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas de la sucursal obtenidas exitosamente")
                .data(Map.of(
                    "branchId", branchId,
                    "totalMemberships", totalProducts,
                    "activeMemberships", activeProducts,
                    "autoRenewalMemberships", autoRenewalProducts,
                    "trialMemberships", trialProducts,
                    "totalValue", totalValue != null ? totalValue : BigDecimal.ZERO
                ))
                .build();
    }
}