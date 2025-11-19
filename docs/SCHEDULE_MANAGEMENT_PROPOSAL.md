# 📅 Propuesta: Gestión de Horarios y Suscripciones

## 🔍 Análisis del Problema Actual

### Situación Actual

**Estructura de Datos:**
1. **`ClassSchedulePattern`**: Patrones recurrentes de horarios (ej: "Lunes 9-10 cada semana")
   - `dayOfWeek`: 1-7
   - `startTime`, `endTime`
   - `recurrent`: true/false (indica si el patrón es recurrente)
   
2. **`ClassSubscription`**: Suscripciones de usuarios
   - `recurrent`: true/false
   - `date`: NULL si es recurrente, fecha específica si no
   - `dayOfWeek`: Día de la semana
   - `startTime`, `endTime`

3. **`Schedule`**: Horarios específicos con fecha exacta (TIMESTAMP)
   - `startTime`: TIMESTAMP (fecha + hora)
   - `endTime`: TIMESTAMP

### Problema Identificado

**Escenario:**
- Usuario reserva: **Lunes 18 Nov 2025, 9:00-10:00** (suscripción específica, `recurrent=false`)
- Cuando consulta sus clases: Ve **"Lunes 9:00-10:00"** en la lista
- **Problema**: El horario sigue apareciendo en semanas futuras aunque solo reservó esa fecha específica

**Causa Raíz:**
- Los horarios se muestran desde `ClassSchedulePattern` (patrones recurrentes)
- No se distingue entre:
  - Patrones recurrentes disponibles (cada semana)
  - Suscripciones específicas (solo una fecha)

---

## 💡 Propuestas de Solución

### **Opción 1: Separar Visualización por Tipo de Suscripción** ⭐ (Recomendada)

**Concepto:** Mostrar horarios diferentes según el tipo de suscripción del usuario.

**Implementación:**

#### 1.1 Endpoint para Horarios del Usuario con Contexto

```java
GET /api/users/{userId}/schedules
```

**Respuesta:**
```json
{
  "recurrentSchedules": [
    {
      "classId": 1,
      "className": "Yoga Vinyasa",
      "dayOfWeek": 1,
      "startTime": "09:00:00",
      "endTime": "10:00:00",
      "type": "RECURRENT",
      "nextOccurrence": "2025-11-25"  // Próximo lunes disponible
    }
  ],
  "specificSchedules": [
    {
      "classId": 1,
      "className": "Yoga Vinyasa",
      "date": "2025-11-18",
      "dayOfWeek": 1,
      "startTime": "09:00:00",
      "endTime": "10:00:00",
      "type": "SPECIFIC",
      "isPast": false
    }
  ]
}
```

**Ventajas:**
- ✅ Distinción clara entre recurrentes y específicos
- ✅ Usuario ve solo lo que realmente tiene reservado
- ✅ No confunde horarios recurrentes con específicos

**Desventajas:**
- ⚠️ Requiere cambios en el frontend para manejar dos listas

---

#### 1.2 Endpoint Unificado con Flag de Tipo

```java
GET /api/users/{userId}/schedules/unified
```

**Respuesta:**
```json
[
  {
    "classId": 1,
    "className": "Yoga Vinyasa",
    "dayOfWeek": 1,
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "subscriptionType": "RECURRENT",  // o "SPECIFIC"
    "date": null,  // null si es recurrente
    "nextOccurrences": ["2025-11-25", "2025-12-02"],  // Próximas fechas si es recurrente
    "subscriptionId": 1
  },
  {
    "classId": 1,
    "className": "Yoga Vinyasa",
    "dayOfWeek": 1,
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "subscriptionType": "SPECIFIC",
    "date": "2025-11-18",  // Fecha específica
    "nextOccurrences": [],
    "subscriptionId": 2
  }
]
```

**Ventajas:**
- ✅ Una sola lista unificada
- ✅ Frontend puede filtrar por tipo fácilmente
- ✅ Incluye información de próximas ocurrencias

---

### **Opción 2: Generar Instancias de Horarios desde Patrones** 🔄

**Concepto:** Generar instancias específicas de horarios desde los patrones recurrentes cuando el usuario se suscribe.

**Implementación:**

Cuando un usuario se suscribe a un patrón recurrente:
1. Crear múltiples `Schedule` (instancias específicas) para las próximas N semanas
2. Crear una `ClassSubscription` recurrente que apunte al patrón
3. Las instancias `Schedule` se generan automáticamente cada semana

**Estructura:**
```java
// Al suscribirse a patrón recurrente
- ClassSubscription (recurrent=true, date=null)
- Schedule (startTime=2025-11-18 09:00, endTime=2025-11-18 10:00)
- Schedule (startTime=2025-11-25 09:00, endTime=2025-11-25 10:00)
- Schedule (startTime=2025-12-02 09:00, endTime=2025-12-02 10:00)
// ... hasta N semanas adelante
```

**Ventajas:**
- ✅ Cada horario es una instancia específica
- ✅ Fácil de consultar por fecha
- ✅ Permite cancelar instancias específicas sin afectar el patrón

**Desventajas:**
- ⚠️ Genera muchos registros en la BD
- ⚠️ Requiere job para generar nuevas instancias cada semana
- ⚠️ Más complejo de mantener

---

### **Opción 3: Endpoint Contextual con Filtro de Fecha** 📅

**Concepto:** Mostrar horarios disponibles considerando el contexto temporal.

**Implementación:**

```java
GET /api/users/{userId}/schedules?date=2025-11-18
```

**Lógica:**
- Si `date` no se proporciona: Muestra solo suscripciones recurrentes + próximas ocurrencias
- Si `date` se proporciona: Muestra suscripciones que aplican para esa fecha específica

**Respuesta:**
```json
{
  "date": "2025-11-18",
  "schedules": [
    {
      "classId": 1,
      "className": "Yoga Vinyasa",
      "dayOfWeek": 1,
      "startTime": "09:00:00",
      "endTime": "10:00:00",
      "appliesToDate": true,  // Esta suscripción aplica para esta fecha
      "subscriptionType": "SPECIFIC",
      "subscriptionId": 2
    }
  ]
}
```

**Ventajas:**
- ✅ Flexible: puede consultar cualquier fecha
- ✅ Muestra solo lo relevante para la fecha consultada
- ✅ Útil para calendarios

**Desventajas:**
- ⚠️ Requiere lógica más compleja para determinar qué aplica

---

### **Opción 4: Expandir Suscripciones Recurrentes en el Backend** 🔀

**Concepto:** Cuando se consultan horarios del usuario, expandir las suscripciones recurrentes en instancias específicas.

**Implementación:**

```java
GET /api/users/{userId}/schedules/expanded?weeksAhead=4
```

**Lógica:**
1. Obtener todas las suscripciones del usuario
2. Para suscripciones recurrentes: generar instancias para las próximas N semanas
3. Para suscripciones específicas: incluir tal cual
4. Retornar lista unificada con fechas específicas

**Respuesta:**
```json
[
  {
    "classId": 1,
    "className": "Yoga Vinyasa",
    "date": "2025-11-18",
    "dayOfWeek": 1,
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "subscriptionType": "SPECIFIC",
    "subscriptionId": 2
  },
  {
    "classId": 1,
    "className": "Yoga Vinyasa",
    "date": "2025-11-25",  // Generado desde suscripción recurrente
    "dayOfWeek": 1,
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "subscriptionType": "RECURRENT",
    "subscriptionId": 1,
    "isGenerated": true  // Indica que fue generado, no es instancia real
  }
]
```

**Ventajas:**
- ✅ Usuario ve todas sus clases con fechas específicas
- ✅ Fácil de mostrar en calendario
- ✅ No requiere cambios en BD

**Desventajas:**
- ⚠️ Cálculo en tiempo real puede ser costoso
- ⚠️ Las instancias generadas no son "reales" (no se pueden cancelar individualmente)

---

## 🎯 Recomendación: Solución Híbrida

### **Combinar Opción 1.2 + Opción 4**

**Estructura Propuesta:**

1. **Endpoint Principal:** `/api/users/{userId}/schedules`
   - Retorna suscripciones con tipo y contexto
   - Incluye flag `subscriptionType` (RECURRENT/SPECIFIC)
   - Para recurrentes: incluye `nextOccurrences` (próximas 4 semanas)

2. **Endpoint Expandido:** `/api/users/{userId}/schedules/expanded?weeksAhead=4`
   - Expande suscripciones recurrentes en instancias específicas
   - Útil para calendarios y vistas mensuales

3. **Endpoint por Fecha:** `/api/users/{userId}/schedules/date/{date}`
   - Muestra solo suscripciones que aplican para esa fecha
   - Útil para vista diaria

**Ejemplo de Respuesta del Endpoint Principal:**

```json
{
  "userId": 3,
  "schedules": [
    {
      "subscriptionId": 1,
      "classId": 2,
      "className": "Pilates",
      "dayOfWeek": 1,
      "startTime": "09:00:00",
      "endTime": "10:00:00",
      "subscriptionType": "RECURRENT",
      "date": null,
      "nextOccurrences": [
        "2025-11-25",
        "2025-12-02",
        "2025-12-09",
        "2025-12-16"
      ],
      "isActive": true
    },
    {
      "subscriptionId": 2,
      "classId": 2,
      "className": "Pilates",
      "dayOfWeek": 1,
      "startTime": "09:00:00",
      "endTime": "10:00:00",
      "subscriptionType": "SPECIFIC",
      "date": "2025-11-18",
      "nextOccurrences": [],
      "isActive": true,
      "isPast": false
    }
  ]
}
```

---

## 📋 Cambios Necesarios

### 1. Nuevo DTO para Respuesta de Horarios del Usuario

```java
@Getter
@Setter
@Builder
public class UserScheduleResponse {
    private Long subscriptionId;
    private Long classId;
    private String className;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private SubscriptionType subscriptionType;  // RECURRENT o SPECIFIC
    private LocalDate date;  // null si es recurrente
    private List<LocalDate> nextOccurrences;  // Próximas fechas si es recurrente
    private Boolean isActive;
    private Boolean isPast;  // Solo para específicas
}

public enum SubscriptionType {
    RECURRENT,
    SPECIFIC
}
```

### 2. Nuevo Método en ClassSubscriptionService

```java
public List<UserScheduleResponse> getUserSchedules(Long userId, Integer weeksAhead) {
    // Obtener todas las suscripciones del usuario
    // Separar por tipo
    // Para recurrentes: calcular próximas ocurrencias
    // Retornar lista unificada
}
```

### 3. Nuevo Endpoint en ClassController

```java
@GetMapping("/user/{userId}/schedules")
public ResponseEntity<List<UserScheduleResponse>> getUserSchedules(
    @PathVariable Long userId,
    @RequestParam(required = false, defaultValue = "4") Integer weeksAhead
) {
    // Implementar lógica
}
```

---

## 🔄 Flujo Propuesto

### Cuando Usuario Consulta Sus Horarios:

1. **Frontend llama:** `GET /api/users/{userId}/schedules`
2. **Backend:**
   - Obtiene todas las suscripciones activas del usuario
   - Separa por tipo (RECURRENT/SPECIFIC)
   - Para recurrentes: calcula próximas N ocurrencias
   - Retorna lista con contexto completo
3. **Frontend:**
   - Muestra lista diferenciada por tipo
   - Para recurrentes: muestra "Cada Lunes" + próximas fechas
   - Para específicas: muestra fecha exacta

### Cuando Usuario Reserva:

**Caso 1: Reserva Recurrente**
```json
POST /api/classes/{id}/subscribe
{
  "userId": 3,
  "dayOfWeek": 1,
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "recurrent": true
}
```
- Crea `ClassSubscription` con `recurrent=true`, `date=null`
- Aparece en lista como "RECURRENT" con próximas ocurrencias

**Caso 2: Reserva Específica**
```json
POST /api/classes/{id}/subscribe
{
  "userId": 3,
  "dayOfWeek": 1,
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "date": "2025-11-18",
  "recurrent": false
}
```
- Crea `ClassSubscription` con `recurrent=false`, `date=2025-11-18`
- Aparece en lista como "SPECIFIC" solo para esa fecha
- No aparece en semanas futuras

---

## ✅ Beneficios de la Solución Propuesta

1. ✅ **Claridad**: Usuario distingue entre recurrentes y específicos
2. ✅ **Precisión**: Solo ve lo que realmente tiene reservado
3. ✅ **Flexibilidad**: Múltiples endpoints para diferentes vistas
4. ✅ **Escalabilidad**: No genera registros innecesarios en BD
5. ✅ **Mantenibilidad**: Lógica clara y separada

---

## 🚀 Próximos Pasos

1. **Discutir y aprobar** la solución propuesta
2. **Implementar** los nuevos endpoints y DTOs
3. **Actualizar frontend** para usar la nueva estructura
4. **Probar** con casos reales de uso

---

## ❓ Preguntas para Discutir

1. ¿Cuántas semanas adelante queremos mostrar para suscripciones recurrentes? (Recomendado: 4-8 semanas)
2. ¿Queremos permitir cancelar instancias específicas de suscripciones recurrentes?
3. ¿Necesitamos un endpoint para "expandir" todas las ocurrencias de una suscripción recurrente?
4. ¿Cómo manejamos suscripciones recurrentes que ya pasaron? ¿Las ocultamos automáticamente?

---

**¿Qué opinas de estas propuestas? ¿Hay algún aspecto que quieras modificar o agregar?**

