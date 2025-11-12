# Guía de Refresh Token - Fit & Flex API

## 📋 Resumen

El sistema ahora implementa **refresh token en el backend** con las siguientes características:

- ✅ **Expiración de tokens**: 24 horas (86400000 ms)
- ✅ **Ventana de gracia**: 7 días después de la expiración para refrescar
- ✅ **Endpoint de refresh**: `/api/auth/refresh-token`
- ✅ **Validación automática**: El backend valida si el token puede ser refrescado

## 🔐 Configuración Actual

### Expiración de Tokens
- **Duración**: 24 horas
- **Configuración**: `app.jwt.expiration-ms=86400000` en `application.properties`

### Ventana de Gracia
- **Duración**: 7 días después de la expiración
- **Propósito**: Permitir refrescar tokens expirados sin requerir login nuevamente

## 🚀 Endpoints Disponibles

### 1. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@fitandflex.com",
  "password": "admin123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 1,
  "email": "admin@fitandflex.com",
  "name": "Super Administrador",
  "role": "SUPER_ADMIN",
  "branchId": 1,
  "branchName": "Fit & Flex Quito Norte",
  "active": true
}
```

### 2. Refresh Token
```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuesta:** (Misma estructura que login)

### 3. Validate Token
```http
GET /api/auth/validate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 💡 Recomendación: Implementación en Frontend

### ✅ **HACER EN BACKEND** (Recomendado)
- ✅ Validación de expiración
- ✅ Refresh automático de tokens
- ✅ Manejo de ventana de gracia
- ✅ Seguridad centralizada

### ❌ **NO HACER EN FRONTEND**
- ❌ Decodificar tokens manualmente
- ❌ Validar expiración en JavaScript
- ❌ Lógica compleja de refresh

## 📱 Implementación Frontend Recomendada

### Estrategia: Interceptor HTTP

```javascript
// Ejemplo con Axios (React/Vue/Angular)

import axios from 'axios';

// Configurar interceptor de respuesta
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Si el error es 401 y no es un intento de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Obtener token actual del storage
        const currentToken = localStorage.getItem('token');
        
        if (!currentToken) {
          // No hay token, redirigir a login
          window.location.href = '/login';
          return Promise.reject(error);
        }

        // Intentar refrescar el token
        const response = await axios.post('/api/auth/refresh-token', {
          token: currentToken
        });

        const { token } = response.data;
        
        // Guardar nuevo token
        localStorage.setItem('token', token);
        
        // Actualizar header de la petición original
        originalRequest.headers['Authorization'] = `Bearer ${token}`;
        
        // Reintentar petición original
        return axios(originalRequest);
        
      } catch (refreshError) {
        // Refresh falló, redirigir a login
        localStorage.removeItem('token');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

### Estrategia: Verificación Proactiva

```javascript
// Verificar y refrescar token antes de que expire
function setupTokenRefresh() {
  const token = localStorage.getItem('token');
  const expiresIn = localStorage.getItem('tokenExpiresIn'); // Guardado en login
  
  if (!token || !expiresIn) return;

  // Calcular tiempo restante (en milisegundos)
  const timeRemaining = expiresIn * 1000 - Date.now();
  
  // Refrescar si quedan menos de 5 minutos
  const refreshThreshold = 5 * 60 * 1000; // 5 minutos
  
  if (timeRemaining < refreshThreshold && timeRemaining > 0) {
    refreshToken();
  }
}

async function refreshToken() {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch('/api/auth/refresh-token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token })
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('token', data.token);
      localStorage.setItem('tokenExpiresIn', data.expiresIn);
      console.log('Token refrescado exitosamente');
    } else {
      // Token no puede ser refrescado, redirigir a login
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
  } catch (error) {
    console.error('Error al refrescar token:', error);
    localStorage.removeItem('token');
    window.location.href = '/login';
  }
}

// Ejecutar verificación cada minuto
setInterval(setupTokenRefresh, 60000);
```

## 🔄 Flujo de Refresh Token

```
1. Usuario hace login
   ↓
2. Backend genera JWT (expira en 24 horas)
   ↓
3. Frontend guarda token y expiresIn
   ↓
4. Usuario hace peticiones con token
   ↓
5. Token expira (después de 24 horas)
   ↓
6. Frontend detecta 401 o verifica expiración
   ↓
7. Frontend llama a /api/auth/refresh-token
   ↓
8. Backend valida:
   - Token es válido (firma correcta)
   - Token está dentro de ventana de gracia (7 días)
   - Usuario existe y está activo
   ↓
9. Backend genera nuevo token (otras 24 horas)
   ↓
10. Frontend actualiza token y continúa
```

## ⚠️ Casos de Error

### Token Inválido
```json
{
  "error": "Invalid JWT token"
}
```
**Solución**: Usuario debe hacer login nuevamente

### Token Fuera de Ventana de Gracia
```json
{
  "error": "Token no puede ser refrescado. Por favor, inicia sesión nuevamente."
}
```
**Solución**: Token expiró hace más de 7 días, requiere login

### Usuario Inactivo
```json
{
  "error": "Usuario inactivo"
}
```
**Solución**: Usuario fue desactivado, requiere reactivación

## 📊 Ventajas de Backend Refresh

1. **Seguridad**: La validación está centralizada y no puede ser manipulada
2. **Simplicidad**: El frontend solo necesita llamar al endpoint
3. **Control**: El backend puede invalidar tokens si es necesario
4. **Auditoría**: Todos los refreshes se registran en logs
5. **Flexibilidad**: Puedes cambiar la lógica sin afectar el frontend

## 🔧 Configuración Avanzada

### Cambiar Ventana de Gracia

Edita `JwtService.java`:
```java
// Cambiar de 7 días a otro valor
long daysSinceExpiration = (now.getTime() - expiration.getTime()) / (1000 * 60 * 60 * 24);
return daysSinceExpiration <= 7; // Cambiar este número
```

### Cambiar Expiración de Tokens

Edita `application.properties`:
```properties
# 24 horas = 86400000 ms
# 12 horas = 43200000 ms
# 48 horas = 172800000 ms
app.jwt.expiration-ms=86400000
```

## 📝 Notas Importantes

1. **Siempre guarda `expiresIn`** en el frontend para saber cuándo refrescar
2. **Maneja errores gracefully** - redirige a login si el refresh falla
3. **No expongas el token** en URLs o logs
4. **Usa HTTPS** en producción para proteger los tokens
5. **Implementa rate limiting** en el endpoint de refresh para prevenir abuso

## 🧪 Testing

### Probar Refresh Token

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fitandflex.com","password":"admin123"}'

# 2. Guardar token de la respuesta

# 3. Esperar 24 horas o modificar expiración para testing

# 4. Refresh token
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"token":"TOKEN_EXPIRADO_AQUI"}'
```

