package com.backoffice.fitandflex.controller;

import com.backoffice.fitandflex.dto.UserDTO;
import com.backoffice.fitandflex.dto.UserClassDTO;
import com.backoffice.fitandflex.dto.UserProductDTO;
import com.backoffice.fitandflex.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de usuarios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Endpoints para gestión de usuarios")
public class UserController {

    private final UserService userService;
    
    /**
     * Helper method para crear Pageable con validaciones
     */
    private org.springframework.data.domain.Pageable createPageable(int page, int size, String sort, String direction) {
        // Validar parámetros
        if (size > 100) size = 100;
        if (size < 1) size = 10;
        if (page < 0) page = 0;
        
        org.springframework.data.domain.Sort.Direction sortDirection = 
            "desc".equalsIgnoreCase(direction) ? 
            org.springframework.data.domain.Sort.Direction.DESC : 
            org.springframework.data.domain.Sort.Direction.ASC;
            
        return org.springframework.data.domain.PageRequest.of(page, size, 
            org.springframework.data.domain.Sort.by(sortDirection, sort));
    }

    @Operation(
        summary = "Crear nuevo usuario",
        description = "Crea un nuevo usuario en el sistema con los datos proporcionados"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.Response.class)
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
            responseCode = "409",
            description = "El email ya está registrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserDTO.Response> createUser(@Valid @RequestBody UserDTO.CreateRequest request) {
        log.info("Creando nuevo usuario: {}", request.getEmail());
        
        UserDTO.Response response = userService.createUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Obtener usuario por ID",
        description = "Obtiene la información completa de un usuario por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<UserDTO.Response> getUserById(@PathVariable Long id) {
        log.info("Obteniendo usuario por ID: {}", id);
        
        UserDTO.Response response = userService.getUserById(id);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener usuario por email",
        description = "Obtiene la información completa de un usuario por su email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.Response.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserDTO.Response> getUserByEmail(@PathVariable String email) {
        log.info("Obteniendo usuario por email: {}", email);
        
        UserDTO.Response response = userService.getUserByEmail(email);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener todos los usuarios",
        description = "Obtiene una lista paginada de todos los usuarios del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Page<UserDTO.Response>> getAllUsers(
            @Parameter(description = "Número de página (por defecto: 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto: 10, máximo: 100)") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar (por defecto: 'id')") 
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Dirección del ordenamiento (asc/desc, por defecto: 'asc')") 
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Obteniendo todos los usuarios con paginación - page: {}, size: {}, sort: {}, direction: {}", 
                page, size, sort, direction);
        
        org.springframework.data.domain.Pageable pageable = createPageable(page, size, sort, direction);
        
        Page<UserDTO.Response> response = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener todos los usuarios (Spring Data paginación)",
        description = "Obtiene una lista paginada de todos los usuarios usando Spring Data paginación automática"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping("/spring-pagination")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Page<UserDTO.Response>> getAllUsersWithSpringPagination(
            @Parameter(description = "Parámetros de paginación de Spring Data") 
            org.springframework.data.domain.Pageable pageable) {
        log.info("Obteniendo todos los usuarios con paginación Spring Data");
        
        Page<UserDTO.Response> response = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener usuarios por sucursal",
        description = "Obtiene todos los usuarios de una sucursal específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios de la sucursal obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<UserDTO.Response>> getUsersByBranch(@PathVariable Long branchId) {
        log.info("Obteniendo usuarios de la sucursal: {}", branchId);
        
        List<UserDTO.Response> response = userService.getUsersByBranch(branchId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener usuarios por rol",
        description = "Obtiene todos los usuarios con un rol específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios del rol obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<UserDTO.Response>> getUsersByRole(@PathVariable String roleName) {
        log.info("Obteniendo usuarios por rol: {}", roleName);
        
        List<UserDTO.Response> response = userService.getUsersByRole(roleName);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener usuarios activos",
        description = "Obtiene todos los usuarios activos del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios activos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        )
    })
    @GetMapping("/active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<List<UserDTO.Response>> getActiveUsers() {
        log.info("Obteniendo usuarios activos");
        
        List<UserDTO.Response> response = userService.getActiveUsers();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Actualizar usuario",
        description = "Actualiza la información de un usuario existente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDTO.Response.class)
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
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<UserDTO.Response> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.UpdateRequest request) {
        log.info("Actualizando usuario: {}", id);
        
        UserDTO.Response response = userService.updateUser(id, request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Cambiar contraseña",
        description = "Cambia la contraseña de un usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Contraseña cambiada exitosamente"
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
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        log.info("Cambiando contraseña para usuario: {}", id);
        
        userService.changePassword(id, request.getNewPassword());
        log.info("Contraseña cambiada exitosamente para usuario: {}", id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Contraseña cambiada exitosamente",
            "userId", id
        ));
    }

    @Operation(
        summary = "Desactivar usuario",
        description = "Desactiva un usuario (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario desactivado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable Long id) {
        log.info("Desactivando usuario: {}", id);
        
        userService.deactivateUser(id);
        log.info("Usuario desactivado exitosamente: {}", id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Usuario desactivado exitosamente",
            "userId", id
        ));
    }

    @Operation(
        summary = "Activar usuario",
        description = "Activa un usuario previamente desactivado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario activado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long id) {
        log.info("Activando usuario: {}", id);
        
        userService.activateUser(id);
        log.info("Usuario activado exitosamente: {}", id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Usuario activado exitosamente",
            "userId", id
        ));
    }

    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina un usuario del sistema (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        log.info("Eliminando usuario: {}", id);
        
        userService.deleteUser(id);
        log.info("Usuario eliminado exitosamente: {}", id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Usuario eliminado exitosamente",
            "userId", id
        ));
    }

    @Operation(
        summary = "Verificar si email existe",
        description = "Verifica si un email ya está registrado en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verificación completada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/email-exists/{email}")
    public ResponseEntity<Map<String, Boolean>> emailExists(@PathVariable String email) {
        log.info("Verificando si email existe: {}", email);
        
        boolean exists = userService.emailExists(email);
        
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(
        summary = "Obtener usuarios (sin autenticación para pruebas)",
        description = "Endpoint de prueba para verificar que la paginación funciona sin autenticación"
    )
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testUsersEndpoint(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Testing users endpoint - page: {}, size: {}, sort: {}, direction: {}", 
                page, size, sort, direction);
        
        return ResponseEntity.ok(Map.of(
            "message", "Users endpoint is working",
            "pagination", Map.of(
                "page", page,
                "size", size,
                "sort", sort,
                "direction", direction
            ),
            "note", "This endpoint is for testing pagination parameters"
        ));
    }

    @Operation(
        summary = "Obtener clases por ID de usuario",
        description = "Obtiene todas las clases asignadas a un usuario a través de sus reservas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clases del usuario obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}/classes")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<List<UserClassDTO.Response>> getUserClasses(@PathVariable Long id) {
        log.info("Obteniendo clases del usuario: {}", id);
        
        List<UserClassDTO.Response> response = userService.getUserClasses(id);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener productos por ID de usuario",
        description = "Obtiene todos los productos asignados a un usuario a través de sus pagos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos del usuario obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @GetMapping("/{id}/products")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<List<UserProductDTO.Response>> getUserProducts(@PathVariable Long id) {
        log.info("Obteniendo productos del usuario: {}", id);
        
        List<UserProductDTO.Response> response = userService.getUserProducts(id);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Actualizar información de clase de usuario",
        description = "Actualiza la información de una clase asignada a un usuario (cambiar clase, horario, fechas, etc.). Solo administradores pueden realizar esta operación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información de clase actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserClassDTO.Response.class)
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
            description = "Acceso denegado - Se requiere rol SUPER_ADMIN o BRANCH_ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario o clase no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{userId}/classes/{reservationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserClassDTO.Response> updateUserClass(
            @PathVariable Long userId,
            @PathVariable Long reservationId,
            @Valid @RequestBody UserClassDTO.UpdateClassRequest request) {
        log.info("Actualizando clase del usuario: {} para reserva: {}", userId, reservationId);
        
        UserClassDTO.Response response = userService.updateUserClass(userId, reservationId, request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Actualizar información de producto de usuario",
        description = "Actualiza la información de un producto asignado a un usuario (cambiar producto, monto, fechas, etc.). Solo administradores pueden realizar esta operación."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información de producto actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProductDTO.Response.class)
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
            description = "Acceso denegado - Se requiere rol SUPER_ADMIN o BRANCH_ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario o producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        )
    })
    @PutMapping("/{userId}/products/{paymentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<UserProductDTO.Response> updateUserProduct(
            @PathVariable Long userId,
            @PathVariable Long paymentId,
            @Valid @RequestBody UserProductDTO.UpdateProductRequest request) {
        log.info("Actualizando producto del usuario: {} para pago: {}", userId, paymentId);
        
        UserProductDTO.Response response = userService.updateUserProduct(userId, paymentId, request);
        
        return ResponseEntity.ok(response);
    }
}
