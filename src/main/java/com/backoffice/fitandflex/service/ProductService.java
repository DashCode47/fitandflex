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
import java.util.stream.Collectors;

/**
 * Servicio para gestión de productos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    /**
     * Crear un nuevo producto
     */
    public ProductDTO.Response createProduct(ProductDTO.CreateRequest request) {
        log.info("Creando producto '{}' para sucursal {}", request.getName(), request.getBranchId());

        // Validar que la sucursal existe
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + request.getBranchId()));

        // Validar que el SKU no existe en la sucursal
        if (request.getSku() != null && productRepository.findBySkuAndBranchId(request.getSku(), request.getBranchId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con SKU '" + request.getSku() + "' en esta sucursal");
        }

        // Crear el producto
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .sku(request.getSku())
                .brand(request.getBrand())
                .size(request.getSize())
                .color(request.getColor())
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .stockQuantity(request.getStockQuantity())
                .minStockLevel(request.getMinStockLevel())
                .maxStockLevel(request.getMaxStockLevel())
                .active(request.getActive())
                .isDigital(request.getIsDigital())
                .requiresApproval(request.getRequiresApproval())
                .isSubscription(request.getIsSubscription())
                .subscriptionDurationDays(request.getSubscriptionDurationDays())
                .imageUrl(request.getImageUrl())
                .tags(request.getTags())
                .weightGrams(request.getWeightGrams())
                .dimensions(request.getDimensions())
                .branch(branch)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Producto creado exitosamente con ID: {}", savedProduct.getId());

        return ProductDTO.fromEntity(savedProduct);
    }

    /**
     * Obtener todos los productos con paginación
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getAllProducts(Pageable pageable) {
        log.info("Obteniendo todos los productos con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener resumen de todos los productos
     */
    @Transactional(readOnly = true)
    public List<ProductDTO.SummaryResponse> getAllProductsSummary() {
        log.info("Obteniendo resumen de todos los productos");
        
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductDTO::fromEntityToSummary)
                .collect(Collectors.toList());
    }

    /**
     * Obtener producto por ID
     */
    @Transactional(readOnly = true)
    public ProductDTO.Response getProductById(Long id) {
        log.info("Obteniendo producto con ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
        return ProductDTO.fromEntity(product);
    }

    /**
     * Obtener productos por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo productos para sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findByBranchId(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos por categoría
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByCategory(String category, Pageable pageable) {
        log.info("Obteniendo productos de categoría '{}' con paginación: {}", category, pageable);
        
        Page<Product> products = productRepository.findByCategory(category, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos por sucursal y categoría
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByBranchAndCategory(Long branchId, String category, Pageable pageable) {
        log.info("Obteniendo productos de sucursal {} y categoría '{}' con paginación: {}", branchId, category, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findByBranchIdAndCategory(branchId, category, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos activos
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getActiveProducts(Pageable pageable) {
        log.info("Obteniendo productos activos con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos activos por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getActiveProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo productos activos de sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findByBranchIdAndActiveTrue(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos por marca
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByBrand(String brand, Pageable pageable) {
        log.info("Obteniendo productos de marca '{}' con paginación: {}", brand, pageable);
        
        Page<Product> products = productRepository.findByBrand(brand, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos por rango de precios
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.info("Obteniendo productos entre ${} y ${} con paginación: {}", minPrice, maxPrice, pageable);
        
        Page<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos con stock bajo
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getLowStockProducts(Pageable pageable) {
        log.info("Obteniendo productos con stock bajo con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findLowStockProducts(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos con stock bajo por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getLowStockProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo productos con stock bajo de sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findLowStockProductsByBranch(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos sin stock
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getOutOfStockProducts(Pageable pageable) {
        log.info("Obteniendo productos sin stock con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findOutOfStockProducts(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos sin stock por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getOutOfStockProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo productos sin stock de sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findOutOfStockProductsByBranch(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos digitales
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getDigitalProducts(Pageable pageable) {
        log.info("Obteniendo productos digitales con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findByIsDigitalTrue(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos de suscripción
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getSubscriptionProducts(Pageable pageable) {
        log.info("Obteniendo productos de suscripción con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findByIsSubscriptionTrue(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Buscar productos por nombre
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProductsByName(String name, Pageable pageable) {
        log.info("Buscando productos por nombre '{}' con paginación: {}", name, pageable);
        
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Buscar productos por descripción
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> searchProductsByDescription(String description, Pageable pageable) {
        log.info("Buscando productos por descripción '{}' con paginación: {}", description, pageable);
        
        Page<Product> products = productRepository.findByDescriptionContainingIgnoreCase(description, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos disponibles para venta
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getAvailableForSaleProducts(Pageable pageable) {
        log.info("Obteniendo productos disponibles para venta con paginación: {}", pageable);
        
        Page<Product> products = productRepository.findAvailableForSale(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Obtener productos disponibles para venta por sucursal
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO.Response> getAvailableForSaleProductsByBranch(Long branchId, Pageable pageable) {
        log.info("Obteniendo productos disponibles para venta de sucursal {} con paginación: {}", branchId, pageable);
        
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        Page<Product> products = productRepository.findAvailableForSaleByBranch(branchId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * Actualizar producto
     */
    public ProductDTO.Response updateProduct(Long id, ProductDTO.UpdateRequest request) {
        log.info("Actualizando producto con ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
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
                throw new IllegalArgumentException("Ya existe un producto con SKU '" + request.getSku() + "' en esta sucursal");
            }
            product.setSku(request.getSku());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getSize() != null) {
            product.setSize(request.getSize());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getMinStockLevel() != null) {
            product.setMinStockLevel(request.getMinStockLevel());
        }
        if (request.getMaxStockLevel() != null) {
            product.setMaxStockLevel(request.getMaxStockLevel());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getIsDigital() != null) {
            product.setIsDigital(request.getIsDigital());
        }
        if (request.getRequiresApproval() != null) {
            product.setRequiresApproval(request.getRequiresApproval());
        }
        if (request.getIsSubscription() != null) {
            product.setIsSubscription(request.getIsSubscription());
        }
        if (request.getSubscriptionDurationDays() != null) {
            product.setSubscriptionDurationDays(request.getSubscriptionDurationDays());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getTags() != null) {
            product.setTags(request.getTags());
        }
        if (request.getWeightGrams() != null) {
            product.setWeightGrams(request.getWeightGrams());
        }
        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Producto actualizado exitosamente con ID: {}", updatedProduct.getId());
        
        return ProductDTO.fromEntity(updatedProduct);
    }

    /**
     * Ajustar stock de producto
     */
    public ProductDTO.Response adjustStock(Long id, ProductDTO.StockAdjustmentRequest request) {
        log.info("Ajustando stock del producto {} con operación {} y cantidad {}", id, request.getOperation(), request.getQuantity());
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
        // Validar que no es un producto digital
        if (product.getIsDigital()) {
            throw new IllegalArgumentException("No se puede ajustar el stock de un producto digital");
        }
        
        switch (request.getOperation().toUpperCase()) {
            case "ADD":
                product.increaseStock(request.getQuantity());
                break;
            case "SUBTRACT":
                product.reduceStock(request.getQuantity());
                break;
            case "SET":
                product.setStockQuantity(request.getQuantity());
                break;
            default:
                throw new IllegalArgumentException("Operación no válida: " + request.getOperation());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Stock ajustado exitosamente para producto con ID: {}", updatedProduct.getId());
        
        return ProductDTO.fromEntity(updatedProduct);
    }

    /**
     * Eliminar producto
     */
    public void deleteProduct(Long id) {
        log.info("Eliminando producto con ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
        // Validar que el producto puede ser eliminado
        if (product.getActive() && product.getStockQuantity() != null && product.getStockQuantity() > 0) {
            throw new IllegalArgumentException("No se puede eliminar un producto activo con stock");
        }
        
        productRepository.delete(product);
        log.info("Producto eliminado exitosamente con ID: {}", id);
    }

    /**
     * Obtener estadísticas de productos
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getProductStats() {
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByActiveTrue();
        long digitalProducts = productRepository.countByIsDigitalTrue();
        long subscriptionProducts = productRepository.countByIsSubscriptionTrue();
        long lowStockProducts = productRepository.countLowStockProducts();
        long outOfStockProducts = productRepository.countOutOfStockProducts();
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(java.util.Map.of(
                    "totalProducts", totalProducts,
                    "activeProducts", activeProducts,
                    "digitalProducts", digitalProducts,
                    "subscriptionProducts", subscriptionProducts,
                    "lowStockProducts", lowStockProducts,
                    "outOfStockProducts", outOfStockProducts
                ))
                .build();
    }

    /**
     * Obtener estadísticas de productos por sucursal
     */
    @Transactional(readOnly = true)
    public CommonDto.SuccessResponse<Object> getProductStatsByBranch(Long branchId) {
        // Validar que la sucursal existe
        if (!branchRepository.existsById(branchId)) {
            throw new IllegalArgumentException("Sucursal no encontrada con ID: " + branchId);
        }
        
        long totalProducts = productRepository.countByBranchId(branchId);
        long activeProducts = productRepository.countByBranchIdAndActiveTrue(branchId);
        long lowStockProducts = productRepository.countLowStockProductsByBranch(branchId);
        long outOfStockProducts = productRepository.countOutOfStockProductsByBranch(branchId);
        BigDecimal inventoryValue = productRepository.sumInventoryValueByBranch(branchId);
        
        return CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas de la sucursal obtenidas exitosamente")
                .data(java.util.Map.of(
                    "branchId", branchId,
                    "totalProducts", totalProducts,
                    "activeProducts", activeProducts,
                    "lowStockProducts", lowStockProducts,
                    "outOfStockProducts", outOfStockProducts,
                    "inventoryValue", inventoryValue != null ? inventoryValue : BigDecimal.ZERO
                ))
                .build();
    }
}
