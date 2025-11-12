# Resumen de Controladores - Fit & Flex API

## 📋 Índice
- [AuthController](#authcontroller)
- [BranchController](#branchcontroller)
- [ClassController](#classcontroller)
- [HomeController](#homecontroller)
- [PaymentController](#paymentcontroller)
- [ProductController](#productcontroller)
- [ReservationController](#reservationcontroller)
- [ScheduleController](#schedulecontroller)
- [UserController](#usercontroller)
- [UserMembershipController](#usermembershipcontroller)

---

## AuthController
**Base URL:** `/api/auth`  
**Descripción:** Endpoints para autenticación de usuarios

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/auth/login` | Iniciar sesión y obtener JWT token | No | `{"email": "user@example.com", "password": "password123"}` |
| POST | `/api/auth/refresh-token` | Refrescar JWT token expirado | No | `{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}` |
| GET | `/api/auth/validate` | Validar si un JWT token es válido | No | - |
| POST | `/api/auth/logout` | Cerrar sesión | No | - |

---

## BranchController
**Base URL:** `/api/branches`  
**Descripción:** Endpoints para gestión de sucursales

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/branches` | Crear nueva sucursal | SUPER_ADMIN | `{"name": "Fit & Flex Quito Norte", "address": "Av. Amazonas N12-123", "city": "Quito", "state": "Pichincha", "country": "Ecuador", "phone": "+593-2-1234567", "email": "quito.norte@fitandflex.com"}` |
| GET | `/api/branches` | Obtener todas las sucursales (paginado) | SUPER_ADMIN |
| GET | `/api/branches/test` | Obtener todas las sucursales (sin auth) | No | - |
| GET | `/api/branches/summary` | Obtener resumen de sucursales | SUPER_ADMIN | - |
| GET | `/api/branches/{id}` | Obtener sucursal por ID | SUPER_ADMIN | - |
| GET | `/api/branches/search` | Buscar sucursal por nombre | SUPER_ADMIN | - |
| GET | `/api/branches/city/{city}` | Buscar sucursales por ciudad | SUPER_ADMIN | - |
| PUT | `/api/branches/{id}` | Actualizar sucursal | SUPER_ADMIN | - |
| DELETE | `/api/branches/{id}` | Eliminar sucursal | SUPER_ADMIN | - |
| GET | `/api/branches/exists` | Verificar si existe sucursal por nombre | SUPER_ADMIN | - |
| GET | `/api/branches/stats` | Obtener estadísticas de sucursales | SUPER_ADMIN | - |

---

## ClassController
**Base URL:** `/api/classes`  
**Descripción:** Endpoints para gestión de clases

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/classes` | Crear nueva clase | SUPER_ADMIN, BRANCH_ADMIN | `{"name": "Yoga Vinyasa", "description": "Clase de yoga dinámico que combina respiración y movimiento", "capacity": 20, "active": true, "branchId": 1}` |
| GET | `/api/classes/{id}` | Obtener clase por ID | No | - |
| GET | `/api/classes` | Obtener todas las clases (paginado) | No | - |
| GET | `/api/classes/branch/{branchId}` | Obtener clases por sucursal | No | - |
| GET | `/api/classes/branch/{branchId}/active` | Obtener clases activas por sucursal | No | - |
| GET | `/api/classes/active` | Obtener clases activas | No | - |
| GET | `/api/classes/search` | Buscar clases por nombre | No | - |
| GET | `/api/classes/capacity` | Buscar clases por capacidad | No | - |
| GET | `/api/classes/available-schedules` | Obtener clases con horarios disponibles | No | - |
| PUT | `/api/classes/{id}` | Actualizar clase | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/classes/{id}/deactivate` | Desactivar clase | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/classes/{id}/activate` | Activar clase | SUPER_ADMIN, BRANCH_ADMIN | - |
| DELETE | `/api/classes/{id}` | Eliminar clase | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/classes/branch/{branchId}/count` | Contar clases activas por sucursal | No | - |
| GET | `/api/classes/{id}/exists` | Verificar si clase existe | No | - |
| GET | `/api/classes/{id}/active` | Verificar si clase está activa | No | - |

---

## HomeController
**Base URL:** `/`  
**Descripción:** Endpoints básicos de información del sistema

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| GET | `/` | Información básica del API | No | - |
| GET | `/api` | Información del API con endpoints | No | - |

---

## PaymentController
**Base URL:** `/api/payments`  
**Descripción:** Endpoints para gestión de pagos

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/payments` | Crear nuevo pago | SUPER_ADMIN, BRANCH_ADMIN, USER | `{"amount": 50.00, "currency": "USD", "paymentMethod": "CASH", "description": "Pago por clase de Octubre", "userId": 1, "reservationId": 1}` |
| GET | `/api/payments` | Obtener todos los pagos (paginado) | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/test` | Obtener todos los pagos (sin auth) | No | - |
| GET | `/api/payments/summary` | Obtener resumen de pagos | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/{id}` | Obtener pago por ID | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/payments/user/{userId}` | Obtener pagos por usuario | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/payments/reservation/{reservationId}` | Obtener pagos por reserva | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/status/{status}` | Obtener pagos por estado | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/method/{paymentMethod}` | Obtener pagos por método de pago | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/branch/{branchId}` | Obtener pagos por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/date-range` | Obtener pagos por rango de fechas | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/amount-range` | Obtener pagos por rango de montos | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/refunds` | Obtener pagos con reembolsos | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/search` | Buscar pagos por descripción | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/payments/{id}` | Actualizar pago | SUPER_ADMIN, BRANCH_ADMIN | - |
| POST | `/api/payments/{id}/complete` | Marcar pago como completado | SUPER_ADMIN, BRANCH_ADMIN | - |
| POST | `/api/payments/{id}/fail` | Marcar pago como fallido | SUPER_ADMIN, BRANCH_ADMIN | - |
| POST | `/api/payments/{id}/refund` | Procesar reembolso | SUPER_ADMIN, BRANCH_ADMIN | - |
| DELETE | `/api/payments/{id}` | Eliminar pago | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/stats` | Obtener estadísticas de pagos | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/payments/stats/user/{userId}` | Obtener estadísticas de pagos por usuario | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/payments/stats/branch/{branchId}` | Obtener estadísticas de pagos por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |

---

## ProductController
**Base URL:** `/api/products`  
**Descripción:** Endpoints para gestión de membresías

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/products` | Crear nueva membresía | SUPER_ADMIN, BRANCH_ADMIN | `{"name": "Membresía Básica Mensual", "description": "Acceso completo a todas las instalaciones y clases grupales", "category": "BASIC", "membershipType": "MENSUAL", "price": 45.99, "durationDays": 30, "maxUsers": 1, "autoRenewal": true, "trialPeriodDays": 7, "benefits": "Acceso a gimnasio, clases grupales, vestuarios", "features": "Sin restricciones de horario, acceso a todas las sucursales", "imageUrl": "https://example.com/membresia-basica.jpg", "branchId": 1}` |
| GET | `/api/products` | Obtener todas las membresías (paginado) | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/products/test` | Obtener todas las membresías (sin auth) | No | - |
| GET | `/api/products/active` | Obtener membresías activas | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/{id}` | Obtener membresía por ID | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/branch/{branchId}` | Obtener membresías por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/products/category/{category}` | Obtener membresías por categoría | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/products/type/{membershipType}` | Obtener membresías por tipo | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/price-range` | Obtener membresías por rango de precios | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/duration/{durationDays}` | Obtener membresías por duración | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/trial` | Obtener membresías con período de prueba | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/auto-renewal` | Obtener membresías con renovación automática | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/search` | Buscar membresías por nombre o descripción | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/search/benefits` | Buscar membresías por beneficios | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/search/features` | Buscar membresías por características | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/products/search/advanced` | Buscar membresías por múltiples criterios | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| PUT | `/api/products/{id}` | Actualizar membresía | SUPER_ADMIN, BRANCH_ADMIN | - |
| DELETE | `/api/products/{id}` | Eliminar membresía | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/products/stats` | Obtener estadísticas de membresías | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/products/stats/branch/{branchId}` | Obtener estadísticas de membresías por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |

---

## ReservationController
**Base URL:** `/api/reservations`  
**Descripción:** Endpoints para gestión de reservas

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/reservations` | Crear nueva reserva | SUPER_ADMIN, BRANCH_ADMIN, USER | `{"userId": 1, "scheduleId": 1}` |
| GET | `/api/reservations` | Obtener todas las reservas (paginado) | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/test` | Obtener todas las reservas (sin auth) | No | - |
| GET | `/api/reservations/summary` | Obtener resumen de reservas | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/{id}` | Obtener reserva por ID | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/reservations/user/{userId}` | Obtener reservas por usuario | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/reservations/schedule/{scheduleId}` | Obtener reservas por horario | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/status/{status}` | Obtener reservas por estado | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/branch/{branchId}` | Obtener reservas por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/class/{classId}` | Obtener reservas por clase | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/user/{userId}/future` | Obtener reservas futuras por usuario | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/reservations/user/{userId}/past` | Obtener reservas pasadas por usuario | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| PUT | `/api/reservations/{id}` | Actualizar reserva | SUPER_ADMIN, BRANCH_ADMIN | - |
| POST | `/api/reservations/{id}/cancel` | Cancelar reserva | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| POST | `/api/reservations/{id}/attendance` | Marcar asistencia | SUPER_ADMIN, BRANCH_ADMIN | - |
| POST | `/api/reservations/{id}/no-show` | Marcar no asistencia | SUPER_ADMIN, BRANCH_ADMIN | - |
| DELETE | `/api/reservations/{id}` | Eliminar reserva | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/exists` | Verificar si existe una reserva | SUPER_ADMIN, BRANCH_ADMIN, USER | - |
| GET | `/api/reservations/stats` | Obtener estadísticas de reservas | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/reservations/stats/user/{userId}` | Obtener estadísticas de reservas por usuario | SUPER_ADMIN, BRANCH_ADMIN, USER | - |

---

## ScheduleController
**Base URL:** `/api/schedules`  
**Descripción:** Endpoints para gestión de horarios

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/schedules` | Crear nuevo horario | SUPER_ADMIN, BRANCH_ADMIN | `{"startTime": "2024-01-15T09:00:00", "endTime": "2024-01-15T10:00:00", "active": true, "classId": 1}` |
| GET | `/api/schedules/{id}` | Obtener horario por ID | No | - |
| GET | `/api/schedules` | Obtener todos los horarios (paginado) | No | - |
| GET | `/api/schedules/class/{classId}` | Obtener horarios por clase | No | - |
| GET | `/api/schedules/class/{classId}/active` | Obtener horarios activos por clase | No | - |
| GET | `/api/schedules/active` | Obtener horarios activos | No | - |
| GET | `/api/schedules/future` | Obtener horarios futuros | No | - |
| GET | `/api/schedules/available` | Obtener horarios disponibles | No | - |
| GET | `/api/schedules/branch/{branchId}` | Obtener horarios por sucursal | No | - |
| GET | `/api/schedules/date-range` | Obtener horarios por rango de fechas | No | - |
| GET | `/api/schedules/date` | Obtener horarios por fecha específica | No | - |
| GET | `/api/schedules/upcoming` | Obtener horarios próximos | No | - |
| GET | `/api/schedules/day-of-week/{dayOfWeek}` | Obtener horarios por día de la semana | No | - |
| PUT | `/api/schedules/{id}` | Actualizar horario | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/schedules/{id}/deactivate` | Desactivar horario | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/schedules/{id}/activate` | Activar horario | SUPER_ADMIN, BRANCH_ADMIN | - |
| DELETE | `/api/schedules/{id}` | Eliminar horario | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/schedules/class/{classId}/count` | Contar horarios activos por clase | No | - |
| GET | `/api/schedules/{id}/exists` | Verificar si horario existe | No | - |
| GET | `/api/schedules/{id}/active` | Verificar si horario está activo | No | - |
| GET | `/api/schedules/{id}/available-spots` | Verificar cupos disponibles | No | - |

---

## UserController
**Base URL:** `/api/users`  
**Descripción:** Endpoints para gestión de usuarios

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/users` | Crear nuevo usuario | SUPER_ADMIN, BRANCH_ADMIN | `{"name": "Juan Pérez", "email": "juan.perez@example.com", "password": "password123", "phone": "+593-99-1234567", "gender": "M", "active": true, "roleName": "USER", "branchId": 1}` |
| GET | `/api/users/{id}` | Obtener usuario por ID | SUPER_ADMIN, BRANCH_ADMIN, Owner | - |
| GET | `/api/users/email/{email}` | Obtener usuario por email | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/users` | Obtener todos los usuarios (paginado) | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/users/spring-pagination` | Obtener usuarios (Spring Data paginación) | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/users/branch/{branchId}` | Obtener usuarios por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/users/role/{roleName}` | Obtener usuarios por rol | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/users/active` | Obtener usuarios activos | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/users/{id}` | Actualizar usuario | SUPER_ADMIN, BRANCH_ADMIN, Owner | - |
| PUT | `/api/users/{id}/password` | Cambiar contraseña | SUPER_ADMIN, BRANCH_ADMIN, Owner | - |
| PUT | `/api/users/{id}/deactivate` | Desactivar usuario | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/users/{id}/activate` | Activar usuario | SUPER_ADMIN, BRANCH_ADMIN | - |
| DELETE | `/api/users/{id}` | Eliminar usuario | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/users/email-exists/{email}` | Verificar si email existe | No | - |
| GET | `/api/users/test` | Endpoint de prueba (sin auth) | No | - |

---

## UserMembershipController
**Base URL:** `/api/user-memberships`  
**Descripción:** Endpoints para gestión de membresías de usuario

### Endpoints:
| Método | URL | Descripción | Autenticación | Request Body |
|--------|-----|-------------|---------------|--------------|
| POST | `/api/user-memberships` | Asignar membresía a usuario | SUPER_ADMIN, BRANCH_ADMIN | `{"userId": 1, "productId": 1, "startDate": "2024-01-15T00:00:00", "endDate": "2024-02-15T23:59:59", "notes": "Membresía premium asignada por promoción"}` |
| GET | `/api/user-memberships` | Obtener todas las membresías (paginado) | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/user-memberships/{id}` | Obtener membresía por ID | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/user-memberships/user/{userId}` | Obtener membresías por usuario | SUPER_ADMIN, BRANCH_ADMIN, Owner | - |
| GET | `/api/user-memberships/user/{userId}/active` | Obtener membresías activas por usuario | SUPER_ADMIN, BRANCH_ADMIN, Owner | - |
| GET | `/api/user-memberships/branch/{branchId}` | Obtener membresías por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/user-memberships/branch/{branchId}/active` | Obtener membresías activas por sucursal | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/user-memberships/expiring` | Obtener membresías que expiran pronto | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/user-memberships/expired` | Obtener membresías vencidas | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/user-memberships/{id}` | Actualizar membresía | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/user-memberships/{id}/status` | Cambiar estado de membresía | SUPER_ADMIN, BRANCH_ADMIN | - |
| PUT | `/api/user-memberships/{id}/extend` | Extender membresía | SUPER_ADMIN, BRANCH_ADMIN | - |
| DELETE | `/api/user-memberships/{id}` | Eliminar membresía | SUPER_ADMIN, BRANCH_ADMIN | - |
| GET | `/api/user-memberships/user/{userId}/has-active` | Verificar si usuario tiene membresía activa | SUPER_ADMIN, BRANCH_ADMIN, Owner | - |
| GET | `/api/user-memberships/user/{userId}/summary` | Obtener resumen de membresías por usuario | SUPER_ADMIN, BRANCH_ADMIN, Owner | - |

---

## 🔐 Roles de Usuario

- **SUPER_ADMIN**: Acceso completo a todas las funcionalidades
- **BRANCH_ADMIN**: Acceso limitado a su sucursal específica
- **USER**: Acceso básico para usuarios finales
- **Owner**: Los usuarios pueden acceder a sus propios datos

## 📝 Notas Importantes

1. **Autenticación**: La mayoría de endpoints requieren autenticación JWT
2. **Paginación**: Los endpoints de listado soportan paginación con parámetros `page`, `size`, `sort`
3. **Soft Delete**: Las eliminaciones son lógicas (soft delete) en lugar de físicas
4. **Documentación**: Todos los endpoints están documentados con Swagger/OpenAPI
5. **Request Bodies**: Los ejemplos mostrados son los campos mínimos requeridos para crear entidades

## 🔧 Detalles de Request Bodies

### Campos Comunes en POST Requests:

- **AuthController**: 
  - Login: `email` (string), `password` (string)
  - Refresh Token: `token` (string) - JWT token expirado a refrescar
- **BranchController**: `name`, `address`, `city`, `state`, `country`, `phone`, `email`
- **ClassController**: `name`, `description`, `capacity` (number), `active` (boolean), `branchId` (number)
- **PaymentController**: `amount` (decimal), `currency`, `paymentMethod`, `description`, `userId`, `reservationId`
- **ProductController**: `name`, `description`, `category`, `membershipType`, `price`, `durationDays`, `branchId`
- **ReservationController**: `userId` (number), `scheduleId` (number)
- **ScheduleController**: `startTime` (datetime), `endTime` (datetime), `active` (boolean), `classId` (number)
- **UserController**: `name`, `email`, `password`, `phone`, `gender`, `active`, `roleName`, `branchId`
- **UserMembershipController**: `userId` (number), `productId` (number), `startDate` (datetime), `endDate` (datetime), `notes` (string)

### Formatos de Datos:
- **Fechas**: Formato ISO 8601 (`YYYY-MM-DDTHH:mm:ss`)
- **Monedas**: Código de 3 letras (USD, EUR, etc.)
- **Géneros**: M (Masculino), F (Femenino)
- **Roles**: SUPER_ADMIN, BRANCH_ADMIN, USER
- **Métodos de Pago**: CASH, CARD, TRANSFER, etc.

## 🚀 Endpoints de Prueba

Varios controladores incluyen endpoints `/test` que no requieren autenticación para facilitar las pruebas durante el desarrollo.

## 📊 Estadísticas Actualizadas

- **Total de Controladores**: 10
- **Total de Endpoints**: Más de 120 endpoints documentados
- **Nuevo Controlador**: UserMembershipController con 15 endpoints para gestión de membresías de usuario
- **Funcionalidades Principales**: 
  - Gestión de membresías asignadas a usuarios
  - Control de fechas de inicio y fin
  - Estados de membresía (ACTIVE, CANCELLED, SUSPENDED, EXPIRED)
  - Extensión de membresías
  - Reportes de membresías que expiran o han vencido
