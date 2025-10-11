# Servicio Temporal de Webhook de Radar

## ⚠️ IMPORTANTE
Este es un servicio **TEMPORAL** creado únicamente para probar webhooks de Radar. **DEBE SER ELIMINADO** después de completar las pruebas.

## Endpoints Disponibles

### 1. Webhook Principal (POST)
```
POST /api/test/radar-webhook
```
- **Propósito**: Recibir webhooks reales de Radar
- **Método**: POST
- **Autenticación**: No requiere autenticación
- **Respuesta**: Devuelve toda la información capturada del webhook

### 2. Verificar Estado (GET)
```
GET /api/test/radar-webhook
```
- **Propósito**: Verificar que el servicio está funcionando
- **Método**: GET
- **Respuesta**: Estado del servicio

### 3. Simulación (POST)
```
POST /api/test/radar-webhook/simulate
```
- **Propósito**: Simular un webhook para pruebas locales
- **Método**: POST
- **Body**: JSON con datos de prueba
- **Respuesta**: Confirmación de simulación

## Información Capturada

El webhook captura **TODA** la información que Radar envía:

1. **Headers HTTP**: Todos los headers de la request
2. **Información de Request**: Método, URI, IP, puerto, etc.
3. **Body Raw**: El contenido completo del webhook
4. **JSON Parseado**: Si el body es JSON válido
5. **Parámetros de Query**: Si los hay
6. **Información Adicional**: User-Agent, timestamps, etc.

## Logs

Todos los webhooks recibidos se registran en los logs con el prefijo:
```
=== WEBHOOK DE RADAR RECIBIDO ===
```

## Configuración en Radar

Para configurar el webhook en Radar, usa esta URL:
```
https://tu-dominio.com/api/test/radar-webhook
```

## Ejemplo de Respuesta

```json
{
  "success": true,
  "message": "Webhook de Radar recibido y procesado exitosamente",
  "timestamp": "2024-01-15T10:30:00",
  "capturedData": {
    "headers": {
      "Content-Type": "application/json",
      "User-Agent": "Radar/1.0",
      "X-Radar-Signature": "sha256=..."
    },
    "requestInfo": {
      "method": "POST",
      "requestURI": "/api/test/radar-webhook",
      "remoteAddr": "192.168.1.100",
      "contentType": "application/json"
    },
    "rawBody": "{\"event\":\"payment.succeeded\",\"data\":{...}}",
    "parsedJson": {
      "event": "payment.succeeded",
      "data": {...}
    },
    "additionalInfo": {
      "timestamp": "2024-01-15T10:30:00",
      "userAgent": "Radar/1.0"
    }
  }
}
```

## Eliminación

Después de completar las pruebas:

1. Eliminar el archivo: `src/main/java/com/backoffice/fitandflex/controller/RadarWebhookTestController.java`
2. Eliminar este archivo: `RADAR_WEBHOOK_TEST_README.md`
3. Desconfigurar el webhook en Radar

## Notas de Seguridad

- Este endpoint **NO requiere autenticación** para facilitar las pruebas
- **NO** debe usarse en producción
- Los logs pueden contener información sensible
- Asegúrate de eliminar el servicio después de las pruebas
