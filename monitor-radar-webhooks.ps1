# Script para monitorear webhooks de Radar en tiempo real
# Uso: .\monitor-radar-webhooks.ps1

Write-Host "🔍 Monitor de Webhooks de Radar" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green
Write-Host ""

# Verificar si la aplicación está ejecutándose
Write-Host "Verificando estado del servicio..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/radar-webhook" -Method GET
    Write-Host "✅ Servicio activo" -ForegroundColor Green
    Write-Host "Total webhooks recibidos: $($response.totalWebhooksReceived)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "❌ Servicio no disponible en http://localhost:8080" -ForegroundColor Red
    Write-Host "Asegúrate de que la aplicación esté ejecutándose" -ForegroundColor Yellow
    Write-Host ""
}

# Función para mostrar el historial de webhooks
function Show-WebhookHistory {
    Write-Host "📋 Historial de Webhooks:" -ForegroundColor Green
    try {
        $history = Invoke-RestMethod -Uri "http://localhost:8080/api/test/radar-webhook/history" -Method GET
        if ($history.totalWebhooks -gt 0) {
            Write-Host "Total de webhooks: $($history.totalWebhooks)" -ForegroundColor Cyan
            Write-Host ""
            foreach ($webhook in $history.webhooks) {
                Write-Host "🔄 Webhook ID: $($webhook.webhookId)" -ForegroundColor Yellow
                Write-Host "   Timestamp: $($webhook.timestamp)" -ForegroundColor Gray
                Write-Host "   IP: $($webhook.requestInfo.remoteAddr)" -ForegroundColor Gray
                if ($webhook.parsedJson) {
                    Write-Host "   Evento: $($webhook.parsedJson.event)" -ForegroundColor White
                }
                Write-Host ""
            }
        } else {
            Write-Host "No hay webhooks recibidos aún" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Error obteniendo historial: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Función para monitorear logs en tiempo real
function Monitor-Logs {
    Write-Host "📊 Monitoreando logs en tiempo real..." -ForegroundColor Green
    Write-Host "Presiona Ctrl+C para detener" -ForegroundColor Yellow
    Write-Host ""
    
    # Buscar archivos de log
    $logFiles = @(
        "logs\application.log",
        "build\logs\application.log",
        "target\logs\application.log"
    )
    
    $logFile = $null
    foreach ($file in $logFiles) {
        if (Test-Path $file) {
            $logFile = $file
            break
        }
    }
    
    if ($logFile) {
        Write-Host "📁 Monitoreando archivo: $logFile" -ForegroundColor Cyan
        Get-Content -Path $logFile -Wait -Tail 10 | Where-Object { $_ -match "WEBHOOK DE RADAR" }
    } else {
        Write-Host "❌ No se encontraron archivos de log" -ForegroundColor Red
        Write-Host "Los logs pueden estar en la consola de la aplicación" -ForegroundColor Yellow
    }
}

# Menú principal
while ($true) {
    Write-Host ""
    Write-Host "Selecciona una opción:" -ForegroundColor White
    Write-Host "1. Ver estado del servicio" -ForegroundColor Cyan
    Write-Host "2. Ver historial de webhooks" -ForegroundColor Cyan
    Write-Host "3. Monitorear logs en tiempo real" -ForegroundColor Cyan
    Write-Host "4. Limpiar historial" -ForegroundColor Cyan
    Write-Host "5. Salir" -ForegroundColor Cyan
    Write-Host ""
    
    $choice = Read-Host "Ingresa tu opción (1-5)"
    
    switch ($choice) {
        "1" {
            Write-Host ""
            try {
                $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/radar-webhook" -Method GET
                Write-Host "✅ Estado del Servicio:" -ForegroundColor Green
                Write-Host "   Status: $($response.status)" -ForegroundColor White
                Write-Host "   Total webhooks: $($response.totalWebhooksReceived)" -ForegroundColor White
                Write-Host "   Timestamp: $($response.timestamp)" -ForegroundColor White
            } catch {
                Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
        "2" {
            Write-Host ""
            Show-WebhookHistory
        }
        "3" {
            Write-Host ""
            Monitor-Logs
        }
        "4" {
            Write-Host ""
            Write-Host "⚠️ ¿Estás seguro de que quieres limpiar el historial? (y/N)" -ForegroundColor Yellow
            $confirm = Read-Host
            if ($confirm -eq "y" -or $confirm -eq "Y") {
                try {
                    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/radar-webhook/history" -Method DELETE
                    Write-Host "✅ Historial limpiado. Total eliminados: $($response.totalCleared)" -ForegroundColor Green
                } catch {
                    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
                }
            }
        }
        "5" {
            Write-Host "👋 ¡Hasta luego!" -ForegroundColor Green
            exit
        }
        default {
            Write-Host "❌ Opción inválida" -ForegroundColor Red
        }
    }
}
