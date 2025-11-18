# ===========================================
# Script para Ejecutar Migración en Railway
# ===========================================
# Este script ejecuta la migración add-day-of-week-to-subscriptions.sql
# en la base de datos PostgreSQL de Railway
# ===========================================

Write-Host ""
Write-Host "🚂 Migración Railway - Add day_of_week to Subscriptions" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que psql esté instalado
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psqlPath) {
    Write-Host "❌ Error: psql no está instalado o no está en el PATH" -ForegroundColor Red
    Write-Host "   Instala PostgreSQL Client Tools desde: https://www.postgresql.org/download/" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ psql encontrado: $($psqlPath.Source)" -ForegroundColor Green
Write-Host ""

# Archivo SQL
$sqlFile = "src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ Error: No se encontró el archivo $sqlFile" -ForegroundColor Red
    Write-Host "   Asegúrate de ejecutar este script desde la raíz del proyecto" -ForegroundColor Yellow
    exit 1
}

Write-Host "📄 Archivo SQL encontrado: $sqlFile" -ForegroundColor Green
Write-Host ""

# Solicitar credenciales de Railway
Write-Host "📋 Ingresa las credenciales de Railway (encuéntralas en Railway Dashboard > PostgreSQL > Variables):" -ForegroundColor Yellow
Write-Host ""

$host = Read-Host "PGHOST (ej: containers-us-west-XXX.railway.app)"
if ([string]::IsNullOrWhiteSpace($host)) {
    Write-Host "❌ Error: PGHOST es requerido" -ForegroundColor Red
    exit 1
}

$port = Read-Host "PGPORT (presiona Enter para usar 5432)"
if ([string]::IsNullOrWhiteSpace($port)) { 
    $port = "5432" 
}

$database = Read-Host "PGDATABASE (ej: railway)"
if ([string]::IsNullOrWhiteSpace($database)) {
    Write-Host "❌ Error: PGDATABASE es requerido" -ForegroundColor Red
    exit 1
}

$username = Read-Host "PGUSER (presiona Enter para usar postgres)"
if ([string]::IsNullOrWhiteSpace($username)) { 
    $username = "postgres" 
}

$password = Read-Host "PGPASSWORD" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

if ([string]::IsNullOrWhiteSpace($passwordPlain)) {
    Write-Host "❌ Error: PGPASSWORD es requerido" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🔌 Conectando a Railway..." -ForegroundColor Cyan
Write-Host "   Host: $host" -ForegroundColor Gray
Write-Host "   Port: $port" -ForegroundColor Gray
Write-Host "   Database: $database" -ForegroundColor Gray
Write-Host "   User: $username" -ForegroundColor Gray
Write-Host ""

# Ejecutar migración
$success = $false
try {
    # Establecer variable de entorno para la contraseña
    $env:PGPASSWORD = $passwordPlain
    
    # Ejecutar psql
    Write-Host "📤 Ejecutando migración..." -ForegroundColor Cyan
    Write-Host ""
    
    $result = & psql -h $host -p $port -U $username -d $database -f $sqlFile 2>&1
    
    # Mostrar resultado
    Write-Host $result
    
    if ($LASTEXITCODE -eq 0) {
        $success = $true
        Write-Host ""
        Write-Host "✅ Migración ejecutada exitosamente!" -ForegroundColor Green
        Write-Host ""
        Write-Host "📝 Próximos pasos:" -ForegroundColor Yellow
        Write-Host "   1. Verifica que la columna day_of_week se creó correctamente" -ForegroundColor Gray
        Write-Host "   2. Reinicia tu aplicación en Railway (si es necesario)" -ForegroundColor Gray
        Write-Host "   3. Prueba crear una suscripción con dayOfWeek" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "❌ Error al ejecutar la migración (código: $LASTEXITCODE)" -ForegroundColor Red
        Write-Host ""
        Write-Host "💡 Posibles causas:" -ForegroundColor Yellow
        Write-Host "   - Credenciales incorrectas" -ForegroundColor Gray
        Write-Host "   - La tabla class_subscriptions no existe (ejecuta primero migration-add-class-subscriptions.sql)" -ForegroundColor Gray
        Write-Host "   - Problemas de conexión a Railway" -ForegroundColor Gray
    }
} catch {
    Write-Host ""
    Write-Host "❌ Error: $_" -ForegroundColor Red
    $success = $false
} finally {
    # Limpiar variable de entorno
    $env:PGPASSWORD = $null
    $passwordPlain = $null
}

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Cyan

if (-not $success) {
    exit 1
}

