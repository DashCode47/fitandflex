# ===========================================
# Script para Ejecutar TODAS las Migraciones en Railway
# ===========================================
# Este script ejecuta todas las migraciones pendientes:
# 1. migration-add-recurrent-field.sql (campo recurrent en class_schedule_patterns)
# 2. migration-add-day-of-week-to-subscriptions.sql (campo day_of_week en class_subscriptions)
# ===========================================

Write-Host ""
Write-Host "🚂 Migraciones Railway - Ejecutar Todas las Migraciones Pendientes" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan
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

# Archivos SQL a ejecutar (en orden)
$migrations = @(
    @{
        File = "src/main/resources/sql/migration-add-recurrent-field.sql"
        Description = "Agregar campo recurrent a class_schedule_patterns"
    },
    @{
        File = "src/main/resources/sql/migration-add-class-subscriptions.sql"
        Description = "Crear tabla class_subscriptions (si no existe)"
    },
    @{
        File = "src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql"
        Description = "Agregar campo day_of_week a class_subscriptions"
    }
)

# Verificar que todos los archivos existan
$missingFiles = @()
foreach ($migration in $migrations) {
    if (-not (Test-Path $migration.File)) {
        $missingFiles += $migration.File
    }
}

if ($missingFiles.Count -gt 0) {
    Write-Host "❌ Error: No se encontraron los siguientes archivos:" -ForegroundColor Red
    foreach ($file in $missingFiles) {
        Write-Host "   - $file" -ForegroundColor Yellow
    }
    Write-Host "   Asegúrate de ejecutar este script desde la raíz del proyecto" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Todos los archivos SQL encontrados" -ForegroundColor Green
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

# Establecer variable de entorno para la contraseña
$env:PGPASSWORD = $passwordPlain

# Ejecutar cada migración
$allSuccess = $true
$migrationNumber = 1

foreach ($migration in $migrations) {
    Write-Host ""
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "📦 Migración $migrationNumber/$($migrations.Count): $($migration.Description)" -ForegroundColor Cyan
    Write-Host "   Archivo: $($migration.File)" -ForegroundColor Gray
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host ""
    
    try {
        Write-Host "📤 Ejecutando migración..." -ForegroundColor Cyan
        Write-Host ""
        
        $result = & psql -h $host -p $port -U $username -d $database -f $migration.File 2>&1
        
        # Mostrar resultado
        Write-Host $result
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Migración ejecutada exitosamente!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "❌ Error al ejecutar la migración (código: $LASTEXITCODE)" -ForegroundColor Red
            $allSuccess = $false
            
            Write-Host ""
            Write-Host "⚠️  ¿Deseas continuar con las siguientes migraciones? (S/N)" -ForegroundColor Yellow
            $continue = Read-Host
            if ($continue -ne "S" -and $continue -ne "s" -and $continue -ne "Y" -and $continue -ne "y") {
                Write-Host "⏹️  Deteniendo ejecución de migraciones..." -ForegroundColor Yellow
                break
            }
        }
    } catch {
        Write-Host ""
        Write-Host "❌ Error: $_" -ForegroundColor Red
        $allSuccess = $false
        
        Write-Host ""
        Write-Host "⚠️  ¿Deseas continuar con las siguientes migraciones? (S/N)" -ForegroundColor Yellow
        $continue = Read-Host
        if ($continue -ne "S" -and $continue -ne "s" -and $continue -ne "Y" -and $continue -ne "y") {
            Write-Host "⏹️  Deteniendo ejecución de migraciones..." -ForegroundColor Yellow
            break
        }
    }
    
    $migrationNumber++
}

# Limpiar variable de entorno
$env:PGPASSWORD = $null
$passwordPlain = $null

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

if ($allSuccess) {
    Write-Host ""
    Write-Host "✅ Todas las migraciones se ejecutaron exitosamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Próximos pasos:" -ForegroundColor Yellow
    Write-Host "   1. Verifica que las columnas se crearon correctamente" -ForegroundColor Gray
    Write-Host "   2. Reinicia tu aplicación en Railway" -ForegroundColor Gray
    Write-Host "   3. Prueba los endpoints de la API" -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host "⚠️  Algunas migraciones fallaron. Revisa los errores arriba." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "💡 Puedes ejecutar migraciones individuales usando:" -ForegroundColor Yellow
    Write-Host "   .\apply-migration-railway.ps1" -ForegroundColor Gray
}

Write-Host ""
Write-Host "====================================================================" -ForegroundColor Cyan

if (-not $allSuccess) {
    exit 1
}

