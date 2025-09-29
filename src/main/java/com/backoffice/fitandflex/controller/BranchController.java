package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.BranchDto;
import com.backoffice.fitandflex.dto.CommonDto;
import com.backoffice.fitandflex.service.BranchService;
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

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de sucursales
 */
@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Endpoints para gestión de sucursales")
@SecurityRequirement(name = "bearerAuth")
public class BranchController {

    private final BranchService branchService;

    /**
     * Crear nueva sucursal (Solo SuperAdmin)
     */
    @Operation(
        summary = "Crear nueva sucursal",
        description = "Crea una nueva sucursal en el sistema. Solo usuarios con rol SUPER_ADMIN pueden realizar esta operación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Sucursal creada exitosamente",
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
            description = "Acceso denegado - Se requiere rol SUPER_ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<BranchDto.Response>> createBranch(
            @Valid @RequestBody BranchDto.CreateRequest request) {
        
        BranchDto.Response branch = branchService.createBranch(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonDto.SuccessResponse.<BranchDto.Response>builder()
                        .success(true)
                        .message("Sucursal creada exitosamente")
                        .data(branch)
                        .build());
    }

    /**
     * Obtener todas las sucursales (paginado) - Solo SuperAdmin
     */
    @Operation(
        summary = "Obtener todas las sucursales",
        description = "Retorna una lista paginada de todas las sucursales del sistema. Solo usuarios con rol SUPER_ADMIN pueden acceder."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de sucursales obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol SUPER_ADMIN"
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<BranchDto.Response>> getAllBranches(
            @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<BranchDto.Response> branches = branchService.getAllBranches(pageable);
        return ResponseEntity.ok(branches);
    }

    /**
     * Obtener resumen de todas las sucursales - Solo SuperAdmin
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<List<BranchDto.SummaryResponse>>> getAllBranchesSummary() {
        List<BranchDto.SummaryResponse> branches = branchService.getAllBranchesSummary();
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<List<BranchDto.SummaryResponse>>builder()
                .success(true)
                .message("Sucursales obtenidas exitosamente")
                .data(branches)
                .build());
    }

    /**
     * Obtener sucursal por ID - SuperAdmin y BranchAdmin (solo su sucursal)
     */
    @Operation(
        summary = "Obtener sucursal por ID",
        description = "Retorna los detalles de una sucursal específica por su ID. Solo usuarios con rol SUPER_ADMIN pueden acceder."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sucursal encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommonDto.SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sucursal no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol SUPER_ADMIN"
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<BranchDto.Response>> getBranchById(
            @Parameter(description = "ID de la sucursal") @PathVariable Long id) {
        BranchDto.Response branch = branchService.getBranchById(id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<BranchDto.Response>builder()
                .success(true)
                .message("Sucursal obtenida exitosamente")
                .data(branch)
                .build());
    }

    /**
     * Buscar sucursal por nombre - Solo SuperAdmin
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<BranchDto.Response>> getBranchByName(
            @RequestParam String name) {
        BranchDto.Response branch = branchService.getBranchByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con nombre: " + name));
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<BranchDto.Response>builder()
                .success(true)
                .message("Sucursal encontrada exitosamente")
                .data(branch)
                .build());
    }

    /**
     * Buscar sucursales por ciudad - Solo SuperAdmin
     */
    @GetMapping("/city/{city}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<List<BranchDto.SummaryResponse>>> getBranchesByCity(
            @PathVariable String city) {
        List<BranchDto.SummaryResponse> branches = branchService.getBranchesByCity(city);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<List<BranchDto.SummaryResponse>>builder()
                .success(true)
                .message("Sucursales encontradas exitosamente")
                .data(branches)
                .build());
    }

    /**
     * Actualizar sucursal - Solo SuperAdmin
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<BranchDto.Response>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchDto.UpdateRequest request) {
        
        BranchDto.Response branch = branchService.updateBranch(id, request);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<BranchDto.Response>builder()
                .success(true)
                .message("Sucursal actualizada exitosamente")
                .data(branch)
                .build());
    }

    /**
     * Eliminar sucursal - Solo SuperAdmin
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<Void>builder()
                .success(true)
                .message("Sucursal eliminada exitosamente")
                .build());
    }

    /**
     * Verificar si existe sucursal por nombre - Solo SuperAdmin
     */
    @GetMapping("/exists")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Boolean>> checkBranchExists(
            @RequestParam String name) {
        boolean exists = branchService.existsByName(name);
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.<Boolean>builder()
                .success(true)
                .message("Verificación completada")
                .data(exists)
                .build());
    }

    /**
     * Obtener estadísticas de sucursales - Solo SuperAdmin
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommonDto.SuccessResponse<Object>> getBranchStats() {
        long totalBranches = branchService.countBranches();
        
        return ResponseEntity.ok(CommonDto.SuccessResponse.builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(Map.of("totalBranches", totalBranches))
                .build());
    }
}