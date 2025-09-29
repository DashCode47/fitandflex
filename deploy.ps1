# ===========================================
# FIT & FLEX - DEPLOYMENT SCRIPT (PowerShell)
# ===========================================

param(
    [switch]$SkipBuild,
    [switch]$Force
)

# Configurar colores para output
$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message, [string]$Color = "Green")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

function Write-Error-Log {
    param([string]$Message)
    Write-Log $Message "Red"
    exit 1
}

function Write-Warning-Log {
    param([string]$Message)
    Write-Log $Message "Yellow"
}

Write-Log "🚀 Iniciando deployment de Fit & Flex API..."

# Verificar que Docker esté instalado
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error-Log "Docker no está instalado. Por favor instala Docker Desktop primero."
}

# Verificar que Docker Compose esté instalado
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Error-Log "Docker Compose no está instalado. Por favor instala Docker Compose primero."
}

# Verificar que el archivo .env existe
if (-not (Test-Path ".env")) {
    Write-Error-Log "Archivo .env no encontrado. Por favor crea el archivo .env basado en env.example"
}

Write-Log "Variables de entorno verificadas correctamente"

# Construir la aplicación si no se especifica SkipBuild
if (-not $SkipBuild) {
    Write-Log "Construyendo la aplicación..."
    & .\gradlew.bat clean build -x test
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Log "Error al construir la aplicación"
    }
    
    # Verificar que el JAR se construyó correctamente
    $jarFiles = Get-ChildItem "build\libs\fitandflex-*.jar" -ErrorAction SilentlyContinue
    if (-not $jarFiles) {
        Write-Error-Log "El archivo JAR no se construyó correctamente"
    }
    
    Write-Log "Aplicación construida exitosamente"
}

# Parar contenedores existentes
Write-Log "Parando contenedores existentes..."
& docker-compose down 2>$null

# Limpiar imágenes antiguas si se especifica Force
if ($Force) {
    Write-Log "Limpiando imágenes antiguas..."
    & docker system prune -f
}

# Construir y levantar los servicios
Write-Log "Construyendo y levantando servicios..."
& docker-compose up --build -d

if ($LASTEXITCODE -ne 0) {
    Write-Error-Log "Error al levantar los servicios con Docker Compose"
}

# Esperar a que los servicios estén listos
Write-Log "Esperando a que los servicios estén listos..."
Start-Sleep -Seconds 30

# Verificar que la aplicación esté funcionando
Write-Log "Verificando estado de la aplicación..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Log "✅ Aplicación desplegada exitosamente!"
        Write-Log "🌐 API disponible en: http://localhost:8080"
        Write-Log "📚 Documentación Swagger: http://localhost:8080/swagger-ui.html"
        Write-Log "🔍 Health Check: http://localhost:8080/actuator/health"
    } else {
        Write-Error-Log "❌ La aplicación no está respondiendo correctamente (Status: $($response.StatusCode))"
    }
} catch {
    Write-Error-Log "❌ La aplicación no está respondiendo correctamente: $($_.Exception.Message)"
}

# Mostrar logs de los contenedores
Write-Log "Mostrando logs de los contenedores..."
& docker-compose logs --tail=50

Write-Log "🎉 Deployment completado exitosamente!"
