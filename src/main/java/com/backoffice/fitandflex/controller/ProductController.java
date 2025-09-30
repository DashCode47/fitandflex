package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.dto.ProductDTO;
import com.backoffice.fitandflex.service.ProductService;
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
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de productos
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints para gestión de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    /**
     * Crear nuevo producto
     */
    @Operation(
        summary = "Crear nuevo producto",
        description = "Crea un nuevo producto en el sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente",
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<ProductDTO.Response>> createProduct(
            @Valid @RequestBody ProductDTO.CreateRequest request) {
        
        ProductDTO.Response product = productService.createProduct(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.SuccessResponse.<ProductDTO.Response>builder()
                        .success(true)
                        .message("Producto creado exitosamente")
                        .data(product)
                        .build());
    }

    /**
     * Obtener todos los productos (paginado)
     */
    @Operation(
        summary = "Obtener todos los productos",
        description = "Retorna una lista paginada de todos los productos del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos obtenida exitosamente",
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
    public ResponseEntity<Page<ProductDTO.Response>> getAllProducts(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener todos los productos (sin autenticación para pruebas)
     */
    @GetMapping("/test")
    public ResponseEntity<Page<ProductDTO.Response>> getAllProductsTest(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener resumen de todos los productos
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<List<ProductDTO.SummaryResponse>>> getAllProductsSummary() {
        List<ProductDTO.SummaryResponse> products = productService.getAllProductsSummary();
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<List<ProductDTO.SummaryResponse>>builder()
                .success(true)
                .message("Productos obtenidos exitosamente")
                .data(products)
                .build());
    }

    /**
     * Obtener producto por ID
     */
    @Operation(
        summary = "Obtener producto por ID",
        description = "Retorna los detalles de un producto específico por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ProductDTO.Response>> getProductById(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        ProductDTO.Response product = productService.getProductById(id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ProductDTO.Response>builder()
                .success(true)
                .message("Producto obtenido exitosamente")
                .data(product)
                .build());
    }

    /**
     * Obtener productos por sucursal
     */
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByBranch(
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
        
        Page<ProductDTO.Response> products = productService.getProductsByBranch(branchId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos por categoría
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByCategory(
            @PathVariable String category,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos por sucursal y categoría
     */
    @GetMapping("/branch/{branchId}/category/{category}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByBranchAndCategory(
            @PathVariable Long branchId,
            @PathVariable String category,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getProductsByBranchAndCategory(branchId, category, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos activos
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getActiveProducts(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos activos por sucursal
     */
    @GetMapping("/branch/{branchId}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getActiveProductsByBranch(
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
        
        Page<ProductDTO.Response> products = productService.getActiveProductsByBranch(branchId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos por marca
     */
    @GetMapping("/brand/{brand}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByBrand(
            @PathVariable String brand,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos por rango de precios
     */
    @GetMapping("/price-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos con stock bajo
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getLowStockProducts(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getLowStockProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos con stock bajo por sucursal
     */
    @GetMapping("/branch/{branchId}/low-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getLowStockProductsByBranch(
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
        
        Page<ProductDTO.Response> products = productService.getLowStockProductsByBranch(branchId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos sin stock
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getOutOfStockProducts(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getOutOfStockProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos sin stock por sucursal
     */
    @GetMapping("/branch/{branchId}/out-of-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getOutOfStockProductsByBranch(
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
        
        Page<ProductDTO.Response> products = productService.getOutOfStockProductsByBranch(branchId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos digitales
     */
    @GetMapping("/digital")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getDigitalProducts(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getDigitalProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos de suscripción
     */
    @GetMapping("/subscription")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<Page<ProductDTO.Response>> getSubscriptionProducts(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getSubscriptionProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar productos por nombre
     */
    @GetMapping("/search/name")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> searchProductsByName(
            @RequestParam String name,
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.searchProductsByName(name, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar productos por descripción
     */
    @GetMapping("/search/description")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> searchProductsByDescription(
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
        
        Page<ProductDTO.Response> products = productService.searchProductsByDescription(description, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos disponibles para venta
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getAvailableForSaleProducts(
            @Parameter(description = "Número de página (por defecto: 0)", required = false) 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false) 
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false) 
            @RequestParam(value = "sort", defaultValue = "id") String sort) {
        
        // Crear Pageable manualmente
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sort));
        
        Page<ProductDTO.Response> products = productService.getAvailableForSaleProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener productos disponibles para venta por sucursal
     */
    @GetMapping("/branch/{branchId}/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getAvailableForSaleProductsByBranch(
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
        
        Page<ProductDTO.Response> products = productService.getAvailableForSaleProductsByBranch(branchId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Actualizar producto
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<ProductDTO.Response>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO.UpdateRequest request) {
        
        ProductDTO.Response product = productService.updateProduct(id, request);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ProductDTO.Response>builder()
                .success(true)
                .message("Producto actualizado exitosamente")
                .data(product)
                .build());
    }

    /**
     * Ajustar stock de producto
     */
    @PostMapping("/{id}/adjust-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<ProductDTO.Response>> adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO.StockAdjustmentRequest request) {
        
        ProductDTO.Response product = productService.adjustStock(id, request);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<ProductDTO.Response>builder()
                .success(true)
                .message("Stock ajustado exitosamente")
                .data(product)
                .build());
    }

    /**
     * Eliminar producto
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Void>> deleteProduct(
            @PathVariable Long id) {
        
        productService.deleteProduct(id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<Void>builder()
                .success(true)
                .message("Producto eliminado exitosamente")
                .build());
    }

    /**
     * Obtener estadísticas de productos
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getProductStats() {
        CommonDto.SuccessResponse<Object> stats = productService.getProductStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener estadísticas de productos por sucursal
     */
    @GetMapping("/stats/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getProductStatsByBranch(
            @PathVariable Long branchId) {
        CommonDto.SuccessResponse<Object> stats = productService.getProductStatsByBranch(branchId);
        return ResponseEntity.ok(stats);
    }
}
