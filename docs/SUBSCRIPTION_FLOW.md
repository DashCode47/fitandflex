# 📋 Flujo para Agregar Usuarios a una Clase (Suscripciones)

## 🎯 Objetivo
Permitir que los usuarios se suscriban/reserven a clases con horarios específicos o recurrentes.

---

## 🔄 Flujo Completo

### **1. Frontend → Backend: Crear Suscripción**

#### **Request:**
```http
POST /api/classes/{classId}/subscribe
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 1,
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "date": "2024-01-15",        // Opcional: si es NULL, es recurrente
  "recurrent": false            // true = se repite cada semana, false = fecha específica
}
```

#### **Validaciones que debe hacer el Backend:**

1. ✅ **Validar que el usuario existe** (`userId`)
2. ✅ **Validar que la clase existe** (`classId`)
3. ✅ **Validar que la clase está activa**
4. ✅ **Validar que no existe ya una suscripción activa** para el mismo usuario, clase, fecha y rango de horas
5. ✅ **Validar capacidad de la clase** (si aplica)
6. ✅ **Validar que el rango de horas es válido** (`startTime < endTime`)
7. ✅ **Si `recurrent = true`**, validar que `date` es NULL
8. ✅ **Si `recurrent = false`**, validar que `date` no es NULL y es una fecha futura

---

### **2. Backend: Procesar Suscripción**

#### **Pasos internos:**

1. **Buscar el usuario** en la base de datos
2. **Buscar la clase** en la base de datos
3. **Verificar si ya existe una suscripción activa** con los mismos datos
4. **Crear el registro** en la tabla `class_subscriptions`:
   ```sql
   INSERT INTO class_subscriptions 
   (user_id, class_id, start_time, end_time, date, recurrent, active, created_at, updated_at)
   VALUES 
   (1, 5, '09:00:00', '10:00:00', '2024-01-15', false, true, NOW(), NOW());
   ```

5. **Retornar respuesta** con los datos de la suscripción creada

---

### **3. Backend → Frontend: Respuesta**

#### **Response (201 Created):**
```json
{
  "success": true,
  "message": "Suscripción creada exitosamente",
  "data": {
    "id": 1,
    "userId": 1,
    "classId": 5,
    "className": "Yoga Vinyasa",
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "date": "2024-01-15",
    "recurrent": false,
    "active": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

## 📊 Estructura de Datos

### **Tabla: `class_subscriptions`**

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `id` | BIGSERIAL | ID único de la suscripción | 1 |
| `user_id` | BIGINT | ID del usuario | 1 |
| `class_id` | BIGINT | ID de la clase | 5 |
| `start_time` | TIME | Hora de inicio | 09:00:00 |
| `end_time` | TIME | Hora de fin | 10:00:00 |
| `date` | DATE | Fecha específica (NULL si es recurrente) | 2024-01-15 |
| `recurrent` | BOOLEAN | Si se repite cada semana | false |
| `active` | BOOLEAN | Si la suscripción está activa | true |
| `created_at` | TIMESTAMP | Fecha de creación | 2024-01-15 10:30:00 |
| `updated_at` | TIMESTAMP | Fecha de última actualización | 2024-01-15 10:30:00 |

---

## 🔍 Casos de Uso

### **Caso 1: Suscripción Recurrente (Semanal)**
Un usuario quiere asistir a Yoga todos los lunes de 9:00 AM a 10:00 AM.

**Request:**
```json
{
  "userId": 1,
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "date": null,
  "recurrent": true
}
```

**Resultado:** Se crea una suscripción que se repite cada semana en el mismo día y horario.

---

### **Caso 2: Suscripción para Fecha Específica**
Un usuario quiere reservar una clase especial el 15 de enero de 2024 de 2:00 PM a 3:00 PM.

**Request:**
```json
{
  "userId": 1,
  "startTime": "14:00:00",
  "endTime": "15:00:00",
  "date": "2024-01-15",
  "recurrent": false
}
```

**Resultado:** Se crea una suscripción solo para esa fecha específica.

---

## 📡 Endpoints Necesarios

### **1. Crear Suscripción**
```http
POST /api/classes/{classId}/subscribe
```
- **Autenticación:** Requerida (USER, BRANCH_ADMIN, SUPER_ADMIN)
- **Body:** `CreateSubscriptionRequest`
- **Response:** `SubscriptionResponse`

---

### **2. Obtener Suscripciones de una Clase**
```http
GET /api/classes/{classId}/subscriptions
```
- **Autenticación:** Requerida
- **Response:** Lista de `SubscriptionResponse`

---

### **3. Obtener Usuarios de una Clase**
```http
GET /api/classes/{classId}/users
```
- **Autenticación:** Requerida
- **Response:** Lista de usuarios únicos suscritos a la clase

---

### **4. Obtener Clases de un Usuario**
```http
GET /api/classes/user/{userId}
```
- **Autenticación:** Requerida
- **Response:** Lista de clases a las que el usuario está suscrito

---

### **5. Obtener Suscripciones de un Usuario**
```http
GET /api/classes/user/{userId}/subscriptions
```
- **Autenticación:** Requerida
- **Response:** Lista de todas las suscripciones del usuario

---

### **6. Cancelar Suscripción**
```http
PUT /api/classes/subscriptions/{subscriptionId}/cancel
```
- **Autenticación:** Requerida
- **Acción:** Marca `active = false`

---

### **7. Eliminar Suscripción**
```http
DELETE /api/classes/subscriptions/{subscriptionId}
```
- **Autenticación:** Requerida (SUPER_ADMIN, BRANCH_ADMIN)
- **Acción:** Elimina físicamente el registro

---

## 🔐 Validaciones Importantes

### **Validación de Duplicados:**
No se puede crear una suscripción si ya existe una activa con:
- Mismo `user_id`
- Mismo `class_id`
- Mismo `date` (o ambos NULL si es recurrente)
- Mismo `start_time` y `end_time`

### **Validación de Capacidad:**
Antes de crear una suscripción, verificar:
```sql
SELECT COUNT(*) 
FROM class_subscriptions 
WHERE class_id = ? 
  AND date = ? 
  AND start_time = ? 
  AND end_time = ? 
  AND active = true;
```

Si el conteo >= `capacity` de la clase → **Error: "Clase llena"**

---

## 📝 Relaciones en el Modelo

### **ClassSubscription → User**
- **Relación:** `@ManyToOne`
- **Propósito:** Saber qué usuario tiene la suscripción

### **ClassSubscription → Class**
- **Relación:** `@ManyToOne`
- **Propósito:** Saber a qué clase pertenece la suscripción

### **User → ClassSubscription**
- **Relación:** `@OneToMany`
- **Propósito:** Saber todas las suscripciones de un usuario

### **Class → ClassSubscription**
- **Relación:** `@OneToMany`
- **Propósito:** Saber todos los usuarios suscritos a una clase

---

## 🚀 Estado Actual

### ✅ **Implementado:**
- ✅ Migración SQL (`migration-add-class-subscriptions.sql`)
- ✅ Tabla `class_subscriptions` creada en la base de datos

### ❌ **Pendiente de Implementar:**
- ❌ Entidad `ClassSubscription.java`
- ❌ Repositorio `ClassSubscriptionRepository.java`
- ❌ Servicio `ClassSubscriptionService.java`
- ❌ DTOs (`CreateSubscriptionRequest`, `SubscriptionResponse`)
- ❌ Endpoints en `ClassController.java`
- ❌ Relaciones en `Class.java` y `User.java`

---

## 📚 Ejemplo de Uso Completo

### **Escenario:** Usuario quiere suscribirse a Yoga Vinyasa

1. **Frontend muestra clases disponibles:**
   ```http
   GET /api/classes
   ```

2. **Usuario selecciona clase y horario:**
   - Clase: "Yoga Vinyasa" (ID: 5)
   - Horario: Lunes 9:00 AM - 10:00 AM
   - Tipo: Recurrente (cada semana)

3. **Frontend envía solicitud:**
   ```http
   POST /api/classes/5/subscribe
   {
     "userId": 1,
     "startTime": "09:00:00",
     "endTime": "10:00:00",
     "date": null,
     "recurrent": true
   }
   ```

4. **Backend valida y crea suscripción**

5. **Frontend muestra confirmación:**
   - "Te has suscrito a Yoga Vinyasa"
   - "Horario: Lunes 9:00 AM - 10:00 AM"
   - "Tipo: Recurrente"

6. **Usuario puede ver sus clases:**
   ```http
   GET /api/classes/user/1
   ```

---

## 🔄 Diferencias: Reservations vs Subscriptions

| Aspecto | Reservations | Subscriptions |
|---------|-------------|---------------|
| **Propósito** | Reservar un horario específico con fecha/hora exacta | Suscribirse a una clase con rango de horas |
| **Tabla** | `reservations` | `class_subscriptions` |
| **Relación** | `user` + `schedule` | `user` + `class` + `timeRange` |
| **Fecha** | Específica (Schedule tiene fecha/hora) | Opcional (puede ser recurrente) |
| **Recurrencia** | No soportada | Soportada (`recurrent = true`) |
| **Uso** | Para clases con horarios específicos | Para suscripciones semanales o específicas |

---

## ✅ Checklist de Implementación

- [ ] Crear entidad `ClassSubscription`
- [ ] Crear repositorio `ClassSubscriptionRepository`
- [ ] Crear servicio `ClassSubscriptionService`
- [ ] Agregar DTOs en `ClassDTO`
- [ ] Agregar endpoints en `ClassController`
- [ ] Agregar relación `@OneToMany` en `Class`
- [ ] Agregar relación `@OneToMany` en `User`
- [ ] Implementar validaciones
- [ ] Probar endpoints
- [ ] Documentar en Swagger/OpenAPI

