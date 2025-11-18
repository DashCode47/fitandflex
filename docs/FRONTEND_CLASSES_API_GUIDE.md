# Guía de Integración - API de Clases

Esta guía proporciona toda la información necesaria para integrar los endpoints de clases en el frontend.

## Tabla de Contenidos

1. [Información General](#información-general)
2. [Autenticación](#autenticación)
3. [Base URL](#base-url)
4. [Estructuras de Datos](#estructuras-de-datos)
5. [Endpoints](#endpoints)
6. [Ejemplos de Código](#ejemplos-de-código)
7. [Manejo de Errores](#manejo-de-errores)

---

## Información General

El módulo de clases permite gestionar las clases disponibles en el sistema. Las clases pueden tener múltiples horarios por día de la semana y están asociadas a una sucursal específica.

**Base URL:** `/api/classes`

---

## Autenticación

Algunos endpoints requieren autenticación mediante JWT token. Los roles permitidos son:
- `SUPER_ADMIN`: Acceso completo
- `BRANCH_ADMIN`: Acceso a clases de su sucursal

**Headers requeridos para endpoints protegidos:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

---

## Base URL

Todas las rutas están bajo: `/api/classes`

**Ejemplo completo:** `https://api.tudominio.com/api/classes`

---

## Estructuras de Datos

### CreateRequest (Crear Clase)

```typescript
interface CreateRequest {
  name: string;                    // Requerido, máximo 100 caracteres
  description?: string;             // Opcional, máximo 500 caracteres
  capacity: number;                // Requerido, mínimo 1
  active?: boolean;                // Opcional, por defecto true
  branchId: number;                // Requerido, ID de la sucursal
  schedules?: DaySchedule[];       // Opcional, horarios de la clase
}

interface DaySchedule {
  dayOfWeek: number;               // Requerido, 1-7 (1=Lunes, 7=Domingo)
  timeRanges: TimeRange[];         // Requerido, al menos un rango
  recurrent?: boolean;             // Opcional, indica si es recurrente (se repite cada semana)
}

interface TimeRange {
  startTime: string;               // Requerido, formato "HH:mm:ss" (ej: "10:00:00")
  endTime: string;                 // Requerido, formato "HH:mm:ss" (ej: "11:30:00")
}
```

**Ejemplo:**
```json
{
  "name": "Yoga Vinyasa",
  "description": "Clase de yoga dinámico que combina respiración y movimiento",
  "capacity": 20,
  "active": true,
  "branchId": 1,
  "schedules": [
    {
      "dayOfWeek": 1,
      "timeRanges": [
        {
          "startTime": "10:00:00",
          "endTime": "11:30:00"
        },
        {
          "startTime": "18:00:00",
          "endTime": "19:30:00"
        }
      ]
    },
    {
      "dayOfWeek": 3,
      "timeRanges": [
        {
          "startTime": "10:00:00",
          "endTime": "11:30:00"
        }
      ]
    }
  ]
}
```

### UpdateRequest (Actualizar Clase)

```typescript
interface UpdateRequest {
  name?: string;                   // Opcional, máximo 100 caracteres
  description?: string;             // Opcional, máximo 500 caracteres
  capacity?: number;                // Opcional, mínimo 1
  active?: boolean;                 // Opcional
  schedules?: DaySchedule[];       // Opcional, actualiza los horarios
}
```

### AssignDayFromDateRequest (Asignar Día desde Fecha)

```typescript
interface AssignDayFromDateRequest {
  date: string;                     // Requerido, formato "yyyy-MM-dd" (ej: "2024-01-15")
  startTime: string;                // Requerido, formato "HH:mm:ss" (ej: "10:00:00")
  endTime: string;                  // Requerido, formato "HH:mm:ss" (ej: "11:30:00")
  recurrent?: boolean;              // Opcional, por defecto false. Si es true, crea patrón recurrente
}
```

**Ejemplo:**
```json
{
  "date": "2024-01-15",
  "startTime": "10:00:00",
  "endTime": "11:30:00",
  "recurrent": true
}
```

### Response (Respuesta Completa)

```typescript
interface ClassResponse {
  id: number;
  name: string;
  description?: string;
  capacity: number;
  active: boolean;
  branch: {
    id: number;
    name: string;
    address?: string;
    city?: string;
    state?: string;
    country?: string;
    phone?: string;
    email?: string;
    createdAt: string;              // ISO 8601 format
    updatedAt: string;              // ISO 8601 format
  };
  createdBy?: {
    id: number;
    name: string;
    email: string;
    active: boolean;
    roleName?: string;
    branchName?: string;
  };
  schedules?: DaySchedule[];        // Lista de horarios con información de recurrencia
  createdAt: string;                // ISO 8601 format (ej: "2024-01-15T10:30:00")
  updatedAt: string;                // ISO 8601 format
}
```

### SummaryResponse (Respuesta Simplificada)

```typescript
interface ClassSummaryResponse {
  id: number;
  name: string;
  capacity: number;
  active: boolean;
  branchName: string;
}
```

### PaginatedResponse (Respuesta Paginada)

```typescript
interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;                   // Página actual (0-indexed)
  first: boolean;
  last: boolean;
  numberOfElements: number;
}
```

---

## Endpoints

### 1. Crear Nueva Clase

**POST** `/api/classes`

**Autenticación:** Requerida (SUPER_ADMIN o BRANCH_ADMIN)

**Request Body:**
```json
{
  "name": "Yoga Vinyasa",
  "description": "Clase de yoga dinámico",
  "capacity": 20,
  "active": true,
  "branchId": 1,
  "schedules": [...]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Yoga Vinyasa",
  "description": "Clase de yoga dinámico",
  "capacity": 20,
  "active": true,
  "branch": {...},
  "createdBy": {...},
  "schedules": [...],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Errores posibles:**
- `400 Bad Request`: Datos inválidos
- `403 Forbidden`: Sin permisos

---

### 2. Obtener Clase por ID

**GET** `/api/classes/{id}`

**Autenticación:** No requerida

**Parámetros:**
- `id` (path): ID de la clase

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Yoga Vinyasa",
  ...
}
```

**Errores posibles:**
- `404 Not Found`: Clase no encontrada

---

### 3. Obtener Todas las Clases (Paginado)

**GET** `/api/classes`

**Autenticación:** No requerida

**Query Parameters:**
- `page` (opcional): Número de página, por defecto `0`
- `size` (opcional): Tamaño de página, por defecto `10`, máximo `100`
- `sort` (opcional): Campo para ordenar, por defecto `name`
- `direction` (opcional): Dirección del ordenamiento (`asc` o `desc`), por defecto `asc`

**Ejemplo:**
```
GET /api/classes?page=0&size=20&sort=name&direction=asc
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Yoga Vinyasa",
      ...
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false,
  "numberOfElements": 20
}
```

---

### 4. Obtener Clases por Sucursal

**GET** `/api/classes/branch/{branchId}`

**Autenticación:** No requerida

**Parámetros:**
- `branchId` (path): ID de la sucursal

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Yoga Vinyasa",
    ...
  }
]
```

---

### 5. Obtener Clases Activas por Sucursal

**GET** `/api/classes/branch/{branchId}/active`

**Autenticación:** No requerida

**Parámetros:**
- `branchId` (path): ID de la sucursal

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Yoga Vinyasa",
    "active": true,
    ...
  }
]
```

---

### 6. Obtener Todas las Clases Activas

**GET** `/api/classes/active`

**Autenticación:** No requerida

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Yoga Vinyasa",
    "active": true,
    ...
  }
]
```

---

### 7. Buscar Clases por Nombre

**GET** `/api/classes/search?name={nombre}`

**Autenticación:** No requerida

**Query Parameters:**
- `name` (requerido): Nombre o parte del nombre a buscar (case insensitive)

**Ejemplo:**
```
GET /api/classes/search?name=yoga
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Yoga Vinyasa",
    ...
  },
  {
    "id": 2,
    "name": "Yoga Restaurativo",
    ...
  }
]
```

---

### 8. Buscar Clases por Capacidad

**GET** `/api/classes/capacity?minCapacity={min}&maxCapacity={max}`

**Autenticación:** No requerida

**Query Parameters:**
- `minCapacity` (opcional): Capacidad mínima
- `maxCapacity` (opcional): Capacidad máxima

**Ejemplos:**
```
GET /api/classes/capacity?minCapacity=10
GET /api/classes/capacity?maxCapacity=30
GET /api/classes/capacity?minCapacity=10&maxCapacity=30
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Yoga Vinyasa",
    "capacity": 20,
    ...
  }
]
```

---

### 9. Obtener Clases con Horarios Disponibles

**GET** `/api/classes/available-schedules`

**Autenticación:** No requerida

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Yoga Vinyasa",
    "schedules": [...],
    ...
  }
]
```

---

### 10. Actualizar Clase

**PUT** `/api/classes/{id}`

**Autenticación:** Requerida (SUPER_ADMIN o BRANCH_ADMIN)

**Parámetros:**
- `id` (path): ID de la clase

**Request Body:**
```json
{
  "name": "Yoga Vinyasa Avanzado",
  "description": "Clase avanzada de yoga",
  "capacity": 15,
  "active": true,
  "schedules": [...]
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Yoga Vinyasa Avanzado",
  ...
}
```

**Errores posibles:**
- `400 Bad Request`: Datos inválidos
- `404 Not Found`: Clase no encontrada

---

### 11. Desactivar Clase

**PUT** `/api/classes/{id}/deactivate`

**Autenticación:** Requerida (SUPER_ADMIN o BRANCH_ADMIN)

**Parámetros:**
- `id` (path): ID de la clase

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Clase desactivada exitosamente",
  "classId": 1
}
```

**Errores posibles:**
- `404 Not Found`: Clase no encontrada

---

### 12. Activar Clase

**PUT** `/api/classes/{id}/activate`

**Autenticación:** Requerida (SUPER_ADMIN o BRANCH_ADMIN)

**Parámetros:**
- `id` (path): ID de la clase

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Clase activada exitosamente",
  "classId": 1
}
```

**Errores posibles:**
- `404 Not Found`: Clase no encontrada

---

### 13. Eliminar Clase

**DELETE** `/api/classes/{id}`

**Autenticación:** Requerida (SUPER_ADMIN o BRANCH_ADMIN)

**Parámetros:**
- `id` (path): ID de la clase

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Clase eliminada exitosamente",
  "classId": 1
}
```

**Errores posibles:**
- `404 Not Found`: Clase no encontrada

---

### 14. Contar Clases Activas por Sucursal

**GET** `/api/classes/branch/{branchId}/count`

**Autenticación:** No requerida

**Parámetros:**
- `branchId` (path): ID de la sucursal

**Response:** `200 OK`
```json
{
  "success": true,
  "count": 15,
  "branchId": 1
}
```

---

### 15. Verificar si Clase Existe

**GET** `/api/classes/{id}/exists`

**Autenticación:** No requerida

**Parámetros:**
- `id` (path): ID de la clase

**Response:** `200 OK`
```json
{
  "success": true,
  "exists": true,
  "classId": 1
}
```

---

### 16. Verificar si Clase Está Activa

**GET** `/api/classes/{id}/active`

**Autenticación:** No requerida

**Parámetros:**
- `id` (path): ID de la clase

**Response:** `200 OK`
```json
{
  "success": true,
  "active": true,
  "classId": 1
}
```

---

### 17. Asignar Día desde Fecha del Calendario

**POST** `/api/classes/{id}/assign-day`

**Autenticación:** Requerida (SUPER_ADMIN o BRANCH_ADMIN)

**Descripción:** Este endpoint permite asignar un día de la semana a una clase desde una fecha seleccionada del calendario. El sistema determina automáticamente el día de la semana de la fecha proporcionada.

- Si `recurrent` es `true`: Crea un patrón recurrente (`ClassSchedulePattern`) que se repetirá cada semana en ese día de la semana.
- Si `recurrent` es `false` o no se proporciona: Crea un horario específico (`Schedule`) solo para esa fecha.

**Parámetros:**
- `id` (path): ID de la clase

**Request Body:**
```json
{
  "date": "2024-01-15",
  "startTime": "10:00:00",
  "endTime": "11:30:00",
  "recurrent": true
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Yoga Vinyasa",
  "description": "Clase de yoga dinámico",
  "capacity": 20,
  "active": true,
  "branch": {...},
  "createdBy": {...},
  "schedules": [
    {
      "dayOfWeek": 1,
      "recurrent": true,
      "timeRanges": [
        {
          "startTime": "10:00:00",
          "endTime": "11:30:00"
        }
      ]
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Notas importantes:**
- El sistema determina automáticamente el día de la semana (1-7) basándose en la fecha proporcionada.
- Si `recurrent` es `true`, el patrón se aplicará cada semana en ese día de la semana.
- Si `recurrent` es `false`, se crea un horario único para esa fecha específica.
- No se pueden crear patrones recurrentes duplicados para el mismo día y rango de horas.
- No se pueden crear horarios específicos que entren en conflicto con otros horarios existentes.

**Errores posibles:**
- `400 Bad Request`: Datos inválidos, conflicto de horarios, o patrón recurrente duplicado
- `403 Forbidden`: Sin permisos
- `404 Not Found`: Clase no encontrada o clase inactiva

---

## Ejemplos de Código

### JavaScript/TypeScript (Fetch API)

```typescript
// Configuración base
const API_BASE_URL = 'https://api.tudominio.com/api/classes';
const token = 'tu-jwt-token-aqui';

// Helper para hacer requests
async function apiRequest(endpoint: string, options: RequestInit = {}) {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Error en la petición');
  }

  return response.json();
}

// Obtener todas las clases (paginado)
async function getAllClasses(page = 0, size = 10, sort = 'name', direction = 'asc') {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sort,
    direction,
  });
  return apiRequest(`?${params.toString()}`);
}

// Obtener clase por ID
async function getClassById(id: number) {
  return apiRequest(`/${id}`);
}

// Obtener clases activas por sucursal
async function getActiveClassesByBranch(branchId: number) {
  return apiRequest(`/branch/${branchId}/active`);
}

// Buscar clases por nombre
async function searchClassesByName(name: string) {
  const params = new URLSearchParams({ name });
  return apiRequest(`/search?${params.toString()}`);
}

// Crear nueva clase
async function createClass(classData: CreateRequest) {
  return apiRequest('', {
    method: 'POST',
    body: JSON.stringify(classData),
  });
}

// Actualizar clase
async function updateClass(id: number, classData: UpdateRequest) {
  return apiRequest(`/${id}`, {
    method: 'PUT',
    body: JSON.stringify(classData),
  });
}

// Activar clase
async function activateClass(id: number) {
  return apiRequest(`/${id}/activate`, {
    method: 'PUT',
  });
}

// Desactivar clase
async function deactivateClass(id: number) {
  return apiRequest(`/${id}/deactivate`, {
    method: 'PUT',
  });
}

// Eliminar clase
async function deleteClass(id: number) {
  return apiRequest(`/${id}`, {
    method: 'DELETE',
  });
}

// Asignar día desde fecha del calendario
async function assignDayFromDate(classId: number, date: string, startTime: string, endTime: string, recurrent: boolean = false) {
  return apiRequest(`/${classId}/assign-day`, {
    method: 'POST',
    body: JSON.stringify({
      date,
      startTime,
      endTime,
      recurrent
    }),
  });
}

// Ejemplo de uso
async function ejemplo() {
  try {
    // Obtener clases activas
    const activeClasses = await getActiveClassesByBranch(1);
    console.log('Clases activas:', activeClasses);

    // Crear nueva clase
    const newClass = await createClass({
      name: 'Pilates Mat',
      description: 'Clase de pilates en colchoneta',
      capacity: 15,
      active: true,
      branchId: 1,
      schedules: [
        {
          dayOfWeek: 1,
          timeRanges: [
            { startTime: '09:00:00', endTime: '10:00:00' }
          ]
        }
      ]
    });
    console.log('Clase creada:', newClass);

    // Asignar día recurrente desde fecha del calendario
    const assignedClass = await assignDayFromDate(
      newClass.id,
      '2024-01-15',  // Lunes
      '10:00:00',
      '11:30:00',
      true  // Recurrente
    );
    console.log('Día asignado (recurrente):', assignedClass);

    // Asignar día específico (no recurrente)
    const specificSchedule = await assignDayFromDate(
      newClass.id,
      '2024-01-20',  // Sábado específico
      '14:00:00',
      '15:30:00',
      false  // No recurrente
    );
    console.log('Horario específico asignado:', specificSchedule);
  } catch (error) {
    console.error('Error:', error);
  }
}
```

### React Hook Example

```typescript
import { useState, useEffect } from 'react';

interface UseClassesOptions {
  branchId?: number;
  activeOnly?: boolean;
  page?: number;
  size?: number;
}

function useClasses(options: UseClassesOptions = {}) {
  const [classes, setClasses] = useState<ClassResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchClasses() {
      try {
        setLoading(true);
        let endpoint = '/api/classes';
        
        if (options.branchId && options.activeOnly) {
          endpoint = `/api/classes/branch/${options.branchId}/active`;
        } else if (options.branchId) {
          endpoint = `/api/classes/branch/${options.branchId}`;
        } else if (options.activeOnly) {
          endpoint = '/api/classes/active';
        }

        const response = await fetch(endpoint);
        if (!response.ok) throw new Error('Error al cargar clases');
        
        const data = await response.json();
        setClasses(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Error desconocido');
      } finally {
        setLoading(false);
      }
    }

    fetchClasses();
  }, [options.branchId, options.activeOnly]);

  return { classes, loading, error };
}

// Uso en componente
function ClassesList() {
  const { classes, loading, error } = useClasses({ branchId: 1, activeOnly: true });

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <ul>
      {classes.map(clazz => (
        <li key={clazz.id}>
          <h3>{clazz.name}</h3>
          <p>Capacidad: {clazz.capacity}</p>
          <p>Sucursal: {clazz.branch.name}</p>
        </li>
      ))}
    </ul>
  );
}
```

### Axios Example

```typescript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'https://api.tudominio.com/api/classes',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Servicios
export const classService = {
  getAll: (params?: { page?: number; size?: number; sort?: string; direction?: string }) =>
    apiClient.get('', { params }),

  getById: (id: number) =>
    apiClient.get(`/${id}`),

  getByBranch: (branchId: number) =>
    apiClient.get(`/branch/${branchId}`),

  getActiveByBranch: (branchId: number) =>
    apiClient.get(`/branch/${branchId}/active`),

  getActive: () =>
    apiClient.get('/active'),

  search: (name: string) =>
    apiClient.get('/search', { params: { name } }),

  getByCapacity: (minCapacity?: number, maxCapacity?: number) =>
    apiClient.get('/capacity', { params: { minCapacity, maxCapacity } }),

  getWithAvailableSchedules: () =>
    apiClient.get('/available-schedules'),

  create: (data: CreateRequest) =>
    apiClient.post('', data),

  update: (id: number, data: UpdateRequest) =>
    apiClient.put(`/${id}`, data),

  activate: (id: number) =>
    apiClient.put(`/${id}/activate`),

  deactivate: (id: number) =>
    apiClient.put(`/${id}/deactivate`),

  delete: (id: number) =>
    apiClient.delete(`/${id}`),

  countByBranch: (branchId: number) =>
    apiClient.get(`/branch/${branchId}/count`),

  exists: (id: number) =>
    apiClient.get(`/${id}/exists`),

  isActive: (id: number) =>
    apiClient.get(`/${id}/active`),

  assignDayFromDate: (id: number, data: AssignDayFromDateRequest) =>
    apiClient.post(`/${id}/assign-day`, data),
};
```

---

## Manejo de Errores

### Códigos de Estado HTTP

- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Datos de entrada inválidos
- `401 Unauthorized`: Token no proporcionado o inválido
- `403 Forbidden`: Sin permisos para realizar la operación
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error del servidor

### Estructura de Error

```typescript
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: {
    field: string;
    message: string;
  }[];
}
```

### Ejemplo de Manejo de Errores

```typescript
async function handleRequest<T>(
  request: () => Promise<T>
): Promise<{ data?: T; error?: string }> {
  try {
    const data = await request();
    return { data };
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const errorMessage = error.response?.data?.message || 'Error desconocido';
      const status = error.response?.status;

      switch (status) {
        case 400:
          return { error: `Datos inválidos: ${errorMessage}` };
        case 401:
          return { error: 'No autenticado. Por favor inicia sesión.' };
        case 403:
          return { error: 'No tienes permisos para realizar esta acción.' };
        case 404:
          return { error: 'Clase no encontrada.' };
        default:
          return { error: `Error del servidor: ${errorMessage}` };
      }
    }
    return { error: 'Error de conexión' };
  }
}
```

---

## Validaciones Importantes

### Al Crear/Actualizar Clase

1. **Nombre**: Requerido, máximo 100 caracteres
2. **Descripción**: Opcional, máximo 500 caracteres
3. **Capacidad**: Requerido al crear, mínimo 1
4. **branchId**: Requerido al crear
5. **Horarios**:
   - `dayOfWeek`: Debe estar entre 1 (Lunes) y 7 (Domingo)
   - `startTime` y `endTime`: Formato "HH:mm:ss"
   - `endTime` debe ser mayor que `startTime`
   - `recurrent`: Opcional, indica si el horario se repite cada semana

### Al Asignar Día desde Fecha

1. **Fecha**: Requerida, formato "yyyy-MM-dd"
2. **Hora inicio y fin**: Requeridas, formato "HH:mm:ss"
3. **Recurrencia**:
   - Si `recurrent` es `true`: Crea un patrón recurrente que se repetirá cada semana en ese día
   - Si `recurrent` es `false` o no se proporciona: Crea un horario específico solo para esa fecha
4. El sistema determina automáticamente el día de la semana (1-7) basándose en la fecha

### Días de la Semana

- `1`: Lunes
- `2`: Martes
- `3`: Miércoles
- `4`: Jueves
- `5`: Viernes
- `6`: Sábado
- `7`: Domingo

---

## Notas Adicionales

1. **Paginación**: El tamaño máximo de página es 100 elementos
2. **Ordenamiento**: Por defecto se ordena por `name` en orden ascendente
3. **Búsqueda**: La búsqueda por nombre es case-insensitive
4. **Soft Delete**: Al eliminar una clase, se realiza un soft delete (no se elimina físicamente)
5. **Horarios**: Los horarios se agrupan por día de la semana en la respuesta
6. **Recurrencia**: 
   - Los patrones recurrentes (`recurrent: true`) se repiten cada semana en el mismo día
   - Los horarios específicos (`recurrent: false`) son únicos para una fecha determinada
   - El campo `recurrent` aparece en la respuesta cuando se obtienen los horarios de una clase
   - Use el endpoint `/assign-day` para asignar días desde el calendario con opción de recurrencia

---

## Recursos Adicionales

- [Swagger UI](http://localhost:8080/swagger-ui.html) - Documentación interactiva de la API
- [Resumen de Controladores](../docs/RESUMEN_CONTROLADORES.md) - Resumen de todos los endpoints

---

**Última actualización:** Enero 2024

