package com.backoffice.fitandflex.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Controlador temporal para probar webhooks de Radar
 * IMPORTANTE: Este controlador es solo para pruebas y debe ser eliminado después
 */
@RestController
@RequestMapping("/api/test/radar-webhook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Radar Webhook Test", description = "Endpoint temporal para probar webhooks de Radar - ELIMINAR DESPUÉS DE PRUEBAS")
public class RadarWebhookTestController {

    private final ObjectMapper objectMapper;
    
    // Almacenar webhooks recibidos en memoria para monitoreo
    private final Map<String, Map<String, Object>> webhookHistory = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> webhookLog = new ArrayList<>();

    /**
     * Endpoint para recibir webhooks de Radar
     * Este endpoint captura TODA la información que Radar envía
     */
    @PostMapping
    @Operation(
        summary = "Recibir webhook de Radar",
        description = "Endpoint temporal para capturar y mostrar toda la información que Radar envía en el webhook"
    )
    public ResponseEntity<Map<String, Object>> receiveRadarWebhook(
            @RequestBody(required = false) String rawBody,
            HttpServletRequest request) {
        
        String webhookId = "webhook_" + System.currentTimeMillis();
        LocalDateTime timestamp = LocalDateTime.now();
        
        log.info("=== WEBHOOK DE RADAR RECIBIDO ===");
        log.info("Webhook ID: {}", webhookId);
        log.info("Timestamp: {}", timestamp);
        log.info("Remote IP: {}", request.getRemoteAddr());
        
        // Crear respuesta con toda la información capturada
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> capturedData = new HashMap<>();
        
        try {
            // 1. Información de headers
            Map<String, String> headers = new HashMap<>();
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                headers.put(headerName, request.getHeader(headerName));
            });
            capturedData.put("headers", headers);
            log.info("Headers recibidos: {}", headers);
            
            // 2. Información de la request
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("method", request.getMethod());
            requestInfo.put("requestURI", request.getRequestURI());
            requestInfo.put("queryString", request.getQueryString());
            requestInfo.put("remoteAddr", request.getRemoteAddr());
            requestInfo.put("remoteHost", request.getRemoteHost());
            requestInfo.put("serverName", request.getServerName());
            requestInfo.put("serverPort", request.getServerPort());
            requestInfo.put("protocol", request.getProtocol());
            requestInfo.put("contentType", request.getContentType());
            requestInfo.put("contentLength", request.getContentLength());
            capturedData.put("requestInfo", requestInfo);
            log.info("Información de request: {}", requestInfo);
            
            // 3. Body raw
            capturedData.put("rawBody", rawBody);
            log.info("Body raw recibido: {}", rawBody);
            
            // 4. Intentar parsear el JSON
            if (rawBody != null && !rawBody.trim().isEmpty()) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(rawBody);
                    capturedData.put("parsedJson", jsonNode);
                    log.info("JSON parseado: {}", jsonNode.toPrettyString());
                } catch (Exception e) {
                    capturedData.put("jsonParseError", e.getMessage());
                    log.warn("Error al parsear JSON: {}", e.getMessage());
                }
            }
            
            // 5. Parámetros de query
            Map<String, String[]> queryParams = request.getParameterMap();
            if (!queryParams.isEmpty()) {
                capturedData.put("queryParameters", queryParams);
                log.info("Parámetros de query: {}", queryParams);
            }
            
            // 6. Información adicional
            Map<String, Object> additionalInfo = new HashMap<>();
            additionalInfo.put("timestamp", LocalDateTime.now().toString());
            additionalInfo.put("userAgent", request.getHeader("User-Agent"));
            additionalInfo.put("accept", request.getHeader("Accept"));
            additionalInfo.put("acceptLanguage", request.getHeader("Accept-Language"));
            additionalInfo.put("acceptEncoding", request.getHeader("Accept-Encoding"));
            additionalInfo.put("connection", request.getHeader("Connection"));
            additionalInfo.put("host", request.getHeader("Host"));
            additionalInfo.put("xForwardedFor", request.getHeader("X-Forwarded-For"));
            additionalInfo.put("xForwardedProto", request.getHeader("X-Forwarded-Proto"));
            additionalInfo.put("xRealIp", request.getHeader("X-Real-IP"));
            capturedData.put("additionalInfo", additionalInfo);
            
            response.put("success", true);
            response.put("message", "Webhook de Radar recibido y procesado exitosamente");
            response.put("webhookId", webhookId);
            response.put("timestamp", timestamp.toString());
            response.put("capturedData", capturedData);
            
            // Almacenar en memoria para monitoreo
            capturedData.put("webhookId", webhookId);
            capturedData.put("timestamp", timestamp.toString());
            webhookHistory.put(webhookId, capturedData);
            webhookLog.add(capturedData);
            
            // Mantener solo los últimos 100 webhooks en memoria
            if (webhookLog.size() > 100) {
                webhookLog.remove(0);
            }
            
            log.info("Webhook almacenado con ID: {}", webhookId);
            log.info("Total webhooks recibidos: {}", webhookLog.size());
            log.info("=== FIN WEBHOOK DE RADAR ===");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error procesando webhook de Radar: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "Error procesando webhook: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("error", e.getClass().getSimpleName());
            response.put("capturedData", capturedData);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint GET para verificar que el servicio está funcionando
     */
    @GetMapping
    @Operation(
        summary = "Verificar estado del webhook",
        description = "Endpoint para verificar que el servicio de webhook está funcionando"
    )
    public ResponseEntity<Map<String, Object>> checkWebhookStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("message", "Servicio de webhook de Radar está funcionando");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("endpoint", "/api/test/radar-webhook");
        response.put("method", "POST");
        response.put("totalWebhooksReceived", webhookLog.size());
        response.put("note", "Este es un servicio temporal para pruebas - ELIMINAR DESPUÉS");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para ver el historial de webhooks recibidos
     */
    @GetMapping("/history")
    @Operation(
        summary = "Ver historial de webhooks",
        description = "Muestra los últimos webhooks recibidos de Radar"
    )
    public ResponseEntity<Map<String, Object>> getWebhookHistory() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Historial de webhooks obtenido");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("totalWebhooks", webhookLog.size());
        response.put("webhooks", webhookLog);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para ver un webhook específico por ID
     */
    @GetMapping("/history/{webhookId}")
    @Operation(
        summary = "Ver webhook específico",
        description = "Muestra los detalles de un webhook específico por su ID"
    )
    public ResponseEntity<Map<String, Object>> getWebhookById(@PathVariable String webhookId) {
        Map<String, Object> webhook = webhookHistory.get(webhookId);
        
        if (webhook == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Webhook no encontrado");
            response.put("webhookId", webhookId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Webhook encontrado");
        response.put("webhookId", webhookId);
        response.put("webhook", webhook);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para limpiar el historial de webhooks
     */
    @DeleteMapping("/history")
    @Operation(
        summary = "Limpiar historial de webhooks",
        description = "Elimina todo el historial de webhooks almacenado en memoria"
    )
    public ResponseEntity<Map<String, Object>> clearWebhookHistory() {
        int totalCleared = webhookLog.size();
        webhookHistory.clear();
        webhookLog.clear();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Historial de webhooks limpiado");
        response.put("totalCleared", totalCleared);
        response.put("timestamp", LocalDateTime.now().toString());
        
        log.info("Historial de webhooks limpiado. Total eliminados: {}", totalCleared);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para simular un webhook (para pruebas locales)
     */
    @PostMapping("/simulate")
    @Operation(
        summary = "Simular webhook de Radar",
        description = "Endpoint para simular un webhook de Radar para pruebas locales"
    )
    public ResponseEntity<Map<String, Object>> simulateRadarWebhook(
            @RequestBody(required = false) Map<String, Object> testData) {
        
        log.info("=== SIMULACIÓN DE WEBHOOK DE RADAR ===");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Webhook simulado exitosamente");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("simulatedData", testData);
        response.put("note", "Esta es una simulación - no es un webhook real de Radar");
        
        log.info("Datos simulados: {}", testData);
        log.info("=== FIN SIMULACIÓN ===");
        
        return ResponseEntity.ok(response);
    }
}
