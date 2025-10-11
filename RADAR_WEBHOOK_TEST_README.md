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
- **Almacenamiento**: Se guarda en memoria para monitoreo

### 2. Verificar Estado (GET)
```
GET /api/test/radar-webhook
```
- **Propósito**: Verificar que el servicio está funcionando
- **Método**: GET
- **Respuesta**: Estado del servicio + contador de webhooks recibidos

### 3. Historial de Webhooks (GET)
```
GET /api/test/radar-webhook/history
```
- **Propósito**: Ver todos los webhooks recibidos
- **Método**: GET
- **Respuesta**: Lista de los últimos 100 webhooks recibidos

### 4. Webhook Específico (GET)
```
GET /api/test/radar-webhook/history/{webhookId}
```
- **Propósito**: Ver detalles de un webhook específico
- **Método**: GET
- **Parámetro**: webhookId (ID único del webhook)
- **Respuesta**: Detalles completos del webhook

### 5. Limpiar Historial (DELETE)
```
DELETE /api/test/radar-webhook/history
```
- **Propósito**: Limpiar el historial de webhooks almacenados
- **Método**: DELETE
- **Respuesta**: Confirmación de limpieza

### 6. Simulación (POST)
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

## 🔍 Monitoreo de Webhooks

### Formas de Verificar si Radar está Disparando el Webhook

#### 1. **Verificar Estado del Servicio**
```bash
GET /api/test/radar-webhook
```
Respuesta incluye:
- Estado del servicio
- **Total de webhooks recibidos** (contador en tiempo real)

#### 2. **Ver Historial de Webhooks**
```bash
GET /api/test/radar-webhook/history
```
Muestra todos los webhooks recibidos con:
- ID único de cada webhook
- Timestamp de recepción
- IP del remitente
- Contenido completo del webhook

#### 3. **Ver Webhook Específico**
```bash
GET /api/test/radar-webhook/history/{webhookId}
```
Para ver detalles completos de un webhook específico.

#### 4. **Logs en Tiempo Real**
Todos los webhooks recibidos se registran en los logs con:
```
=== WEBHOOK DE RADAR RECIBIDO ===
Webhook ID: webhook_1705312345678
Timestamp: 2024-01-15T10:30:00
Remote IP: 192.168.1.100
Headers recibidos: {...}
Body raw recibido: {...}
Webhook almacenado con ID: webhook_1705312345678
Total webhooks recibidos: 5
=== FIN WEBHOOK DE RADAR ===
```

#### 5. **Comandos para Monitorear Logs**

**En desarrollo local:**
```bash
# Ver logs en tiempo real
tail -f logs/application.log | grep "WEBHOOK DE RADAR"

# Ver solo los webhooks recibidos
grep "WEBHOOK DE RADAR" logs/application.log
```

**En producción (si tienes acceso al servidor):**
```bash
# Ver logs en tiempo real
tail -f /var/log/application.log | grep "WEBHOOK DE RADAR"

# Ver webhooks de las últimas 24 horas
grep "WEBHOOK DE RADAR" /var/log/application.log | grep "$(date +%Y-%m-%d)"
```

### 📊 Indicadores de que Radar está Funcionando

1. **Contador Incrementa**: El endpoint `/api/test/radar-webhook` muestra `totalWebhooksReceived > 0`
2. **Logs Aparecen**: Se ven mensajes con `=== WEBHOOK DE RADAR RECIBIDO ===`
3. **Historial se Llena**: `/api/test/radar-webhook/history` muestra webhooks recibidos
4. **IPs de Radar**: Los webhooks vienen de IPs conocidas de Radar

### 🚨 Solución de Problemas

**Si no recibes webhooks:**
1. Verifica que el endpoint esté configurado correctamente en Radar
2. Revisa que la URL sea accesible públicamente
3. Verifica que no haya firewall bloqueando las requests
4. Comprueba los logs de errores de la aplicación

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
