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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controlador para gestión de membresías
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Memberships", description = "Endpoints para gestión de membresías")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;
    
    /**
     * Helper method para crear Pageable
     */
    private Pageable createPageable(int page, int size, String sort) {
        return PageRequest.of(page, size, Sort.by(sort));
    }

    /**
     * Crear nueva membresía
     */
    @Operation(
        summary = "Crear nueva membresía",
        description = "Crea una nueva membresía en una sucursal específica."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Membresía creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o conflicto de SKU",
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

        log.info("Creando nueva membresía '{}' para sucursal {}", request.getName(), request.getBranchId());
        ProductDTO.Response product = productService.createProduct(request);
        log.info("Membresía creada exitosamente con ID: {}", product.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.SuccessResponse.<ProductDTO.Response>builder()
                        .success(true)
                        .message("Membresía creada exitosamente")
                        .data(product)
                        .build());
    }

    /**
     * Obtener todas las membresías (paginado)
     */
    @Operation(
        summary = "Obtener todas las membresías",
        description = "Retorna una lista paginada de todas las membresías del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de membresías obtenida exitosamente",
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

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener todas las membresías (sin autenticación para pruebas)
     */
    @GetMapping("/test")
    public ResponseEntity<Page<ProductDTO.Response>> getAllProductsTest(
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresías activas
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

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresía por ID
     */
    @Operation(
        summary = "Obtener membresía por ID",
        description = "Retorna los detalles de una membresía específica por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Membresía encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Membresía no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<CommonDto.SuccessResponse<ProductDTO.Response>> getProductById(
            @Parameter(description = "ID de la membresía") @PathVariable Long id) {
        ProductDTO.Response product = productService.getProductById(id);

        return ResponseEntity.ok(CommonDto.SuccessResponse.<ProductDTO.Response>builder()
                .success(true)
                .message("Membresía obtenida exitosamente")
                .data(product)
                .build());
    }

    /**
     * Obtener membresías por sucursal
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

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getProductsByBranch(branchId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresías por categoría
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

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresías por tipo
     */
    @GetMapping("/type/{membershipType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByMembershipType(
            @PathVariable String membershipType,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getProductsByMembershipType(membershipType, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresías por rango de precios
     */
    @GetMapping("/price-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByPriceRange(
            @Parameter(description = "Precio mínimo", required = true)
            @RequestParam BigDecimal minPrice,
            @Parameter(description = "Precio máximo", required = true)
            @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresías por duración
     */
    @GetMapping("/duration/{durationDays}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsByDuration(
            @PathVariable Integer durationDays,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getProductsByDuration(durationDays, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresías con período de prueba
     */
    @GetMapping("/trial")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsWithTrialPeriod(
            @Parameter(description = "Días mínimos de prueba (por defecto: 1)", required = false)
            @RequestParam(value = "minDays", defaultValue = "1") Integer minTrialDays,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getProductsWithTrialPeriod(minTrialDays, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener membresías con renovación automática
     */
    @GetMapping("/auto-renewal")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> getProductsWithAutoRenewal(
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.getProductsWithAutoRenewal(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar membresías por nombre o descripción
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> searchProducts(
            @Parameter(description = "Término de búsqueda para nombre o descripción", required = true)
            @RequestParam String query,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.searchProductsByName(query, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar membresías por beneficios
     */
    @GetMapping("/search/benefits")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> searchProductsByBenefits(
            @Parameter(description = "Término de búsqueda en beneficios", required = true)
            @RequestParam String benefit,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.searchProductsByBenefits(benefit, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar membresías por características
     */
    @GetMapping("/search/features")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> searchProductsByFeatures(
            @Parameter(description = "Término de búsqueda en características", required = true)
            @RequestParam String feature,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.searchProductsByFeatures(feature, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Buscar membresías por múltiples criterios
     */
    @GetMapping("/search/advanced")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN', 'USER')")
    public ResponseEntity<Page<ProductDTO.Response>> searchProductsByCriteria(
            @Parameter(description = "ID de sucursal", required = false)
            @RequestParam(required = false) Long branchId,
            @Parameter(description = "Categoría", required = false)
            @RequestParam(required = false) String category,
            @Parameter(description = "Tipo de membresía", required = false)
            @RequestParam(required = false) String membershipType,
            @Parameter(description = "Solo activas", required = false)
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Solo con renovación automática", required = false)
            @RequestParam(required = false) Boolean autoRenewal,
            @Parameter(description = "Precio mínimo", required = false)
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Precio máximo", required = false)
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Duración mínima en días", required = false)
            @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Duración máxima en días", required = false)
            @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Número de página (por defecto: 0)", required = false)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10)", required = false)
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: id)", required = false)
            @RequestParam(value = "sort", defaultValue = "id") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ProductDTO.Response> products = productService.searchProductsByCriteria(
                branchId, category, membershipType, active, autoRenewal, 
                minPrice, maxPrice, minDuration, maxDuration, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Actualizar membresía
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<ProductDTO.Response>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO.UpdateRequest request) {

        log.info("Actualizando membresía con ID: {}", id);
        ProductDTO.Response product = productService.updateProduct(id, request);
        log.info("Membresía actualizada exitosamente con ID: {}", product.getId());

        return ResponseEntity.ok(CommonDto.SuccessResponse.<ProductDTO.Response>builder()
                .success(true)
                .message("Membresía actualizada exitosamente")
                .data(product)
                .build());
    }

    /**
     * Eliminar membresía
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Void>> deleteProduct(
            @PathVariable Long id) {

        log.info("Eliminando membresía con ID: {}", id);
        productService.deleteProduct(id);
        log.info("Membresía eliminada exitosamente con ID: {}", id);

        return ResponseEntity.ok(CommonDto.SuccessResponse.<Void>builder()
                .success(true)
                .message("Membresía eliminada exitosamente")
                .build());
    }

    /**
     * Obtener estadísticas de membresías
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getProductStats() {
        CommonDto.SuccessResponse<Object> stats = productService.getProductStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener estadísticas de membresías por sucursal
     */
    @GetMapping("/stats/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getProductStatsByBranch(
            @PathVariable Long branchId) {
        CommonDto.SuccessResponse<Object> stats = productService.getProductStatsByBranch(branchId);
        return ResponseEntity.ok(stats);
    }
}