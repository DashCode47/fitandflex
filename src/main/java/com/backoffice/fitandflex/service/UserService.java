package com.backoffice.fitandflex.service;

import com.backoffice.fitandflex.dto.UserDTO;
import com.backoffice.fitandflex.dto.UserClassDTO;
import com.backoffice.fitandflex.dto.UserProductDTO;
import com.backoffice.fitandflex.dto.UserMembershipDTO;
import com.backoffice.fitandflex.entity.Branch;
import com.backoffice.fitandflex.entity.Role;
import com.backoffice.fitandflex.entity.User;
import com.backoffice.fitandflex.entity.Reservation;
import com.backoffice.fitandflex.entity.Payment;
import com.backoffice.fitandflex.entity.Schedule;
import com.backoffice.fitandflex.entity.ReservationStatus;
import com.backoffice.fitandflex.entity.Payment.PaymentStatus;
import com.backoffice.fitandflex.entity.Payment.PaymentMethod;
import com.backoffice.fitandflex.repository.BranchRepository;
import com.backoffice.fitandflex.repository.RoleRepository;
import com.backoffice.fitandflex.repository.UserRepository;
import com.backoffice.fitandflex.repository.ReservationRepository;
import com.backoffice.fitandflex.repository.PaymentRepository;
import com.backoffice.fitandflex.repository.ScheduleRepository;
import com.backoffice.fitandflex.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserMembershipRepository userMembershipRepository;

    /**
     * Crear un nuevo usuario
     */
    public UserDTO.Response createUser(UserDTO.CreateRequest request) {
        log.info("Creando nuevo usuario: {}", request.getEmail());
        
        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
        }

        // Buscar rol
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + request.getRoleName()));

        // Buscar sucursal si se proporciona
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + request.getBranchId()));
        }

        // Crear usuario
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .active(request.getActive() != null ? request.getActive() : true)
                .role(role)
                .branch(branch)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario creado exitosamente: {}", savedUser.getEmail());
        
        return UserDTO.Response.fromEntity(savedUser);
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UserDTO.Response getUserById(Long id) {
        log.info("Buscando usuario por ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        UserDTO.Response response = UserDTO.Response.fromEntity(user);
        
        // Obtener membresías del usuario
        List<UserMembershipDTO.Response> memberships = userMembershipRepository.findByUserId(id).stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        response.setMemberships(memberships);
        
        return response;
    }

    /**
     * Obtener usuario por email
     */
    @Transactional(readOnly = true)
    public UserDTO.Response getUserByEmail(String email) {
        log.info("Buscando usuario por email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        
        UserDTO.Response response = UserDTO.Response.fromEntity(user);
        
        // Obtener membresías del usuario
        List<UserMembershipDTO.Response> memberships = userMembershipRepository.findByUserId(user.getId()).stream()
                .map(UserMembershipDTO.Response::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        response.setMemberships(memberships);
        
        return response;
    }

    /**
     * Obtener todos los usuarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<UserDTO.Response> getAllUsers(Pageable pageable) {
        log.info("Obteniendo todos los usuarios con paginación");
        
        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserDTO.Response::fromEntity);
    }

    /**
     * Obtener usuarios por sucursal
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> getUsersByBranch(Long branchId) {
        log.info("Obteniendo usuarios de la sucursal: {}", branchId);
        
        List<User> users = userRepository.findByBranchId(branchId);
        return users.stream()
                .map(UserDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Obtener usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> getUsersByRole(String roleName) {
        log.info("Obteniendo usuarios por rol: {}", roleName);
        
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(UserDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Actualizar usuario
     */
    public UserDTO.Response updateUser(Long id, UserDTO.UpdateRequest request) {
        log.info("Actualizando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        // Actualizar campos si se proporcionan
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Validar que el nuevo email no exista
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("El email ya está registrado: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + request.getRoleName()));
            user.setRole(role);
        }
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + request.getBranchId()));
            user.setBranch(branch);
        }

        User updatedUser = userRepository.save(user);
        log.info("Usuario actualizado exitosamente: {}", updatedUser.getEmail());
        
        return UserDTO.Response.fromEntity(updatedUser);
    }

    /**
     * Cambiar contraseña
     */
    public void changePassword(Long id, String newPassword) {
        log.info("Cambiando contraseña para usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Contraseña cambiada exitosamente para usuario: {}", user.getEmail());
    }

    /**
     * Desactivar usuario
     */
    public void deactivateUser(Long id) {
        log.info("Desactivando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Usuario desactivado exitosamente: {}", user.getEmail());
    }

    /**
     * Activar usuario
     */
    public void activateUser(Long id) {
        log.info("Activando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        user.setActive(true);
        userRepository.save(user);
        
        log.info("Usuario activado exitosamente: {}", user.getEmail());
    }

    /**
     * Eliminar usuario (soft delete)
     */
    public void deleteUser(Long id) {
        log.info("Eliminando usuario: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        
        // Soft delete - solo desactivar
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Usuario eliminado exitosamente: {}", user.getEmail());
    }

    /**
     * Verificar si un email existe
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Obtener usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UserDTO.Response> getActiveUsers() {
        log.info("Obteniendo usuarios activos");
        
        List<User> users = userRepository.findByActiveTrue();
        return users.stream()
                .map(UserDTO.Response::fromEntity)
                .toList();
    }

    /**
     * Verificar si un usuario es propietario de un recurso
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long userId, String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
            return user.getId().equals(userId);
        } catch (Exception e) {
            log.warn("Error verificando propiedad del usuario: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtener clases por ID de usuario
     */
    @Transactional(readOnly = true)
    public List<UserClassDTO.Response> getUserClasses(Long userId) {
        log.info("Obteniendo clases del usuario: {}", userId);
        
        // Validar que el usuario existe
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        // Obtener reservas del usuario
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        
        return reservations.stream()
                .map(UserClassDTO.Response::fromReservation)
                .toList();
    }

    /**
     * Obtener productos por ID de usuario
     */
    @Transactional(readOnly = true)
    public List<UserProductDTO.Response> getUserProducts(Long userId) {
        log.info("Obteniendo productos del usuario: {}", userId);
        
        // Validar que el usuario existe
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        // Obtener pagos del usuario
        List<Payment> payments = paymentRepository.findByUserId(userId);
        
        return payments.stream()
                .map(payment -> {
                    // Si hay una reserva asociada, intentar obtener el producto
                    if (payment.getReservation() != null) {
                        // En el modelo actual no hay relación directa entre clase y producto
                        // Por ahora retornamos solo la información del pago
                        return UserProductDTO.Response.fromPayment(payment);
                    } else {
                        return UserProductDTO.Response.fromPayment(payment);
                    }
                })
                .toList();
    }

    /**
     * Actualizar información de clase de usuario
     */
    public UserClassDTO.Response updateUserClass(Long userId, Long reservationId, UserClassDTO.UpdateClassRequest request) {
        log.info("Actualizando clase del usuario: {} para reserva: {}", userId, reservationId);
        
        // Validar que el usuario existe
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        // Obtener la reserva
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        
        // Verificar que la reserva pertenece al usuario
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("La reserva no pertenece al usuario especificado");
        }
        
        // Actualizar campos si se proporcionan
        if (request.getNewScheduleId() != null) {
            Schedule newSchedule = scheduleRepository.findById(request.getNewScheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado: " + request.getNewScheduleId()));
            reservation.setSchedule(newSchedule);
        }
        
        if (request.getNewReservationDate() != null) {
            reservation.setReservationDate(request.getNewReservationDate());
        }
        
        if (request.getNewReservationStatus() != null) {
            try {
                ReservationStatus status = ReservationStatus.valueOf(request.getNewReservationStatus());
                reservation.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado de reserva inválido: " + request.getNewReservationStatus());
            }
        }
        
        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Clase del usuario actualizada exitosamente");
        
        return UserClassDTO.Response.fromReservation(updatedReservation);
    }

    /**
     * Actualizar información de producto de usuario
     */
    public UserProductDTO.Response updateUserProduct(Long userId, Long paymentId, UserProductDTO.UpdateProductRequest request) {
        log.info("Actualizando producto del usuario: {} para pago: {}", userId, paymentId);
        
        // Validar que el usuario existe
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));
        
        // Obtener el pago
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado: " + paymentId));
        
        // Verificar que el pago pertenece al usuario
        if (!payment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("El pago no pertenece al usuario especificado");
        }
        
        // Actualizar campos si se proporcionan
        if (request.getNewPaymentAmount() != null) {
            payment.setAmount(request.getNewPaymentAmount());
        }
        
        if (request.getNewPaymentDate() != null) {
            payment.setPaymentDate(request.getNewPaymentDate());
        }
        
        if (request.getNewPaymentStatus() != null) {
            try {
                PaymentStatus status = PaymentStatus.valueOf(request.getNewPaymentStatus());
                payment.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado de pago inválido: " + request.getNewPaymentStatus());
            }
        }
        
        if (request.getNewPaymentMethod() != null) {
            try {
                PaymentMethod method = PaymentMethod.valueOf(request.getNewPaymentMethod());
                payment.setPaymentMethod(method);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Método de pago inválido: " + request.getNewPaymentMethod());
            }
        }
        
        if (request.getNewPaymentDescription() != null) {
            payment.setDescription(request.getNewPaymentDescription());
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Producto del usuario actualizado exitosamente");
        
        return UserProductDTO.Response.fromPayment(updatedPayment);
    }
}
