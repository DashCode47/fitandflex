# 📋 Resumen del Flujo de Suscripciones

## 🎯 Objetivo
Permitir que los usuarios se suscriban a clases con horarios específicos, distinguiendo por día de la semana.

---

## 🔄 Flujo Completo de Suscripción

### **1. Frontend → Backend: Crear Suscripción**

#### **Request:**
```http
POST /api/classes/{classId}/subscribe
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 3,
  "startTime": "18:00:00",
  "endTime": "19:30:00",
  "date": "2025-11-18",      // Opcional: si se proporciona, se usa para calcular dayOfWeek
  "dayOfWeek": 1,             // Opcional: 1=Lunes, 7=Domingo. Si no se proporciona y hay date, se calcula automáticamente
  "recurrent": false          // true = recurrente (se repite cada semana), false = fecha específica
}
```

#### **Validaciones del Backend:**

1. ✅ Usuario existe
2. ✅ Clase existe y está activa
3. ✅ Rango de horas válido (`startTime < endTime`)
4. ✅ Lógica `recurrent`/`date`:
   - Si `recurrent = true` → `date` debe ser `null`
   - Si `recurrent = false` → `date` es obligatorio
5. ✅ Cálculo de `dayOfWeek`:
   - Si viene `dayOfWeek` en el request → se usa
   - Si no viene pero hay `date` → se calcula desde la fecha
   - Si es recurrente sin `date` ni `dayOfWeek` → error
6. ✅ No existe suscripción duplicada (mismo usuario, clase, día, fecha y horario)
7. ✅ Capacidad disponible (solo para fechas específicas)

---

### **2. Backend: Procesar Suscripción**

#### **Pasos internos:**

1. Validar datos de entrada
2. Calcular `dayOfWeek`:
   - Desde `request.dayOfWeek` (si viene)
   - O desde `date.getDayOfWeek().getValue()` (si hay fecha)
3. Verificar duplicados considerando:
   - `user_id`
   - `class_id`
   - `day_of_week` ← **NUEVO: distingue por día**
   - `date` (o NULL si es recurrente)
   - `start_time` y `end_time`
4. Crear registro en `class_subscriptions`:
   ```sql
   INSERT INTO class_subscriptions 
   (user_id, class_id, day_of_week, start_time, end_time, date, recurrent, active, ...)
   VALUES 
   (3, 2, 1, '18:00:00', '19:30:00', '2025-11-18', false, true, ...);
   ```
5. Retornar respuesta con datos de la suscripción

---

### **3. Backend → Frontend: Respuesta**

#### **Response (201 Created):**
```json
{
  "success": true,
  "message": "Suscripción creada exitosamente",
  "data": {
    "id": 1,
    "userId": 3,
    "classId": 2,
    "className": "Pilates sabados",
    "startTime": "18:00:00",
    "endTime": "19:30:00",
    "date": "2025-11-18",
    "dayOfWeek": 1,        // ← NUEVO: día de la semana
    "recurrent": false,
    "active": true,
    "createdAt": "2025-11-17T20:30:00",
    "updatedAt": "2025-11-17T20:30:00"
  }
}
```

---

## 📊 Estructura de Datos

### **Tabla: `class_subscriptions`**

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `id` | BIGSERIAL | ID único | 1 |
| `user_id` | BIGINT | ID del usuario | 3 |
| `class_id` | BIGINT | ID de la clase | 2 |
| `day_of_week` | INTEGER | **Día de la semana (1-7)** | 1 (Lunes) |
| `start_time` | TIME | Hora inicio | 18:00:00 |
| `end_time` | TIME | Hora fin | 19:30:00 |
| `date` | DATE | Fecha específica (NULL si recurrente) | 2025-11-18 |
| `recurrent` | BOOLEAN | Si se repite cada semana | false |
| `active` | BOOLEAN | Si está activa | true |

**Constraint único:** `(user_id, class_id, day_of_week, date, start_time, end_time)`

---

## 🔍 Conteo de Suscripciones por Horario

### **Antes (PROBLEMA):**
```json
{
  "dayOfWeek": 1,
  "timeRanges": [{
    "startTime": "10:00:00",
    "endTime": "11:30:00",
    "subscriptionCount": 2  // ← Contaba TODAS las suscripciones con ese horario
  }]
},
{
  "dayOfWeek": 2,
  "timeRanges": [{
    "startTime": "10:00:00",
    "endTime": "11:30:00",
    "subscriptionCount": 2  // ← Mismo conteo (INCORRECTO)
  }]
}
```

### **Ahora (SOLUCIONADO):**
```json
{
  "dayOfWeek": 1,
  "timeRanges": [{
    "startTime": "10:00:00",
    "endTime": "11:30:00",
    "subscriptionCount": 1  // ← Solo suscripciones del Lunes
  }]
},
{
  "dayOfWeek": 2,
  "timeRanges": [{
    "startTime": "10:00:00",
    "endTime": "11:30:00",
    "subscriptionCount": 1  // ← Solo suscripciones del Martes
  }]
}
```

---

## 📡 Endpoints Disponibles

### **1. Crear Suscripción**
```http
POST /api/classes/{classId}/subscribe
Body: {
  "userId": 3,
  "startTime": "18:00:00",
  "endTime": "19:30:00",
  "date": "2025-11-18",    // Opcional: se usa para calcular dayOfWeek
  "dayOfWeek": 1,           // Opcional: si no viene, se calcula desde date
  "recurrent": false
}
```

### **2. Ver Suscripciones de una Clase**
```http
GET /api/classes/{id}/subscriptions
```

### **3. Ver Usuarios de una Clase**
```http
GET /api/classes/{id}/users
```

### **4. Ver Usuarios de un Horario Específico**
```http
GET /api/classes/{id}/users/time?startTime=18:00:00&endTime=19:30:00&date=2025-11-18
```

### **5. Ver Clases de un Usuario**
```http
GET /api/classes/user/{userId}
```

### **6. Ver Suscripciones de un Usuario**
```http
GET /api/classes/user/{userId}/subscriptions
```

### **7. Cancelar Suscripción**
```http
PUT /api/classes/subscriptions/{subscriptionId}/cancel
```

### **8. Eliminar Suscripción**
```http
DELETE /api/classes/subscriptions/{subscriptionId}
```

---

## 🔑 Puntos Clave

### **1. Día de la Semana (`dayOfWeek`)**
- **1** = Lunes
- **2** = Martes
- **3** = Miércoles
- **4** = Jueves
- **5** = Viernes
- **6** = Sábado
- **7** = Domingo

### **2. Cálculo Automático de `dayOfWeek`**
- Si envías `date` → se calcula automáticamente
- Si envías `dayOfWeek` explícitamente → se usa ese valor
- Si es recurrente sin `date` → `dayOfWeek` es obligatorio

### **3. Distinción por Día**
- Cada suscripción está asociada a un día específico
- El conteo (`subscriptionCount`) ahora es por día y horario
- No se mezclan suscripciones de diferentes días

---

## 📝 Ejemplos de Uso

### **Ejemplo 1: Suscripción Recurrente (Lunes cada semana)**
```json
{
  "userId": 3,
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "dayOfWeek": 1,        // Lunes
  "date": null,
  "recurrent": true
}
```

### **Ejemplo 2: Suscripción para Fecha Específica**
```json
{
  "userId": 3,
  "startTime": "18:00:00",
  "endTime": "19:30:00",
  "date": "2025-11-18",  // Se calcula dayOfWeek = 1 (Lunes) automáticamente
  "recurrent": false
}
```

### **Ejemplo 3: Suscripción con dayOfWeek Explícito**
```json
{
  "userId": 3,
  "startTime": "18:00:00",
  "endTime": "19:30:00",
  "dayOfWeek": 2,        // Martes (se usa este valor)
  "date": "2025-11-18",  // Aunque la fecha es Lunes, se usa dayOfWeek=2
  "recurrent": false
}
```

---

## ✅ Beneficios de la Implementación

1. ✅ **Distinción por día**: Cada horario muestra suscripciones solo de su día específico
2. ✅ **Conteo preciso**: `subscriptionCount` refleja usuarios por día y horario
3. ✅ **Validación de duplicados**: Evita suscripciones duplicadas considerando el día
4. ✅ **Flexibilidad**: Soporta recurrentes y específicas
5. ✅ **Cálculo automático**: `dayOfWeek` se calcula desde `date` si no se proporciona

---

## 🚀 Próximos Pasos

1. **Ejecutar migración SQL** para agregar `day_of_week`:
   ```sql
   -- Ejecutar: migration-add-day-of-week-to-subscriptions.sql
   ```

2. **Reiniciar aplicación** para que los cambios surtan efecto

3. **Probar suscripciones** con el nuevo campo `dayOfWeek`

---

## 📚 Resumen Visual del Flujo

```
Usuario selecciona clase y horario
         ↓
Frontend envía POST /api/classes/{id}/subscribe
         ↓
Backend valida y calcula dayOfWeek
         ↓
Backend verifica duplicados (incluyendo dayOfWeek)
         ↓
Backend crea suscripción con dayOfWeek
         ↓
Backend retorna suscripción creada
         ↓
Frontend muestra confirmación
```

---

## 🔧 Migración Requerida

**IMPORTANTE:** Debes ejecutar la migración SQL antes de usar las suscripciones:

```sql
-- Archivo: src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql
```

Esta migración:
- Agrega columna `day_of_week` a `class_subscriptions`
- Actualiza registros existentes calculando el día desde `date` o `created_at`
- Actualiza el constraint único para incluir `day_of_week`
- Crea índices para mejorar el rendimiento

