# ===========================================
# Script para Verificar y Corregir Migraciones en Railway
# ===========================================
# Este script verifica el estado de las migraciones y las ejecuta si faltan
# ===========================================

Write-Host ""
Write-Host "🔍 Verificación y Corrección de Migraciones Railway" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
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

# Solicitar credenciales de Railway
Write-Host "📋 Ingresa las credenciales de Railway:" -ForegroundColor Yellow
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
Write-Host ""

# Establecer variable de entorno para la contraseña
$env:PGPASSWORD = $passwordPlain

# Función para ejecutar SQL y obtener resultado
function Execute-SQL {
    param(
        [string]$sql,
        [string]$host,
        [string]$port,
        [string]$username,
        [string]$database
    )
    
    $result = & psql -h $host -p $port -U $username -d $database -t -A -c $sql 2>&1
    return $result
}

# Verificar estado de las migraciones
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📊 Verificando estado de las migraciones..." -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Verificar columna recurrent en class_schedule_patterns
Write-Host "1️⃣  Verificando columna 'recurrent' en 'class_schedule_patterns'..." -ForegroundColor Yellow
$recurrentExists = Execute-SQL -sql "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'class_schedule_patterns' AND column_name = 'recurrent';" -host $host -port $port -username $username -database $database

if ($recurrentExists -eq "1") {
    Write-Host "   ✅ La columna 'recurrent' existe" -ForegroundColor Green
    $needsRecurrentMigration = $false
} else {
    Write-Host "   ❌ La columna 'recurrent' NO existe" -ForegroundColor Red
    $needsRecurrentMigration = $true
}

Write-Host ""

# Verificar tabla class_subscriptions
Write-Host "2️⃣  Verificando tabla 'class_subscriptions'..." -ForegroundColor Yellow
$subscriptionsTableExists = Execute-SQL -sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'class_subscriptions';" -host $host -port $port -username $username -database $database

if ($subscriptionsTableExists -eq "1") {
    Write-Host "   ✅ La tabla 'class_subscriptions' existe" -ForegroundColor Green
    $needsSubscriptionsTable = $false
} else {
    Write-Host "   ❌ La tabla 'class_subscriptions' NO existe" -ForegroundColor Red
    $needsSubscriptionsTable = $true
}

Write-Host ""

# Verificar columna day_of_week en class_subscriptions
if (-not $needsSubscriptionsTable) {
    Write-Host "3️⃣  Verificando columna 'day_of_week' en 'class_subscriptions'..." -ForegroundColor Yellow
    $dayOfWeekExists = Execute-SQL -sql "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'class_subscriptions' AND column_name = 'day_of_week';" -host $host -port $port -username $username -database $database
    
    if ($dayOfWeekExists -eq "1") {
        Write-Host "   ✅ La columna 'day_of_week' existe" -ForegroundColor Green
        $needsDayOfWeekMigration = $false
    } else {
        Write-Host "   ❌ La columna 'day_of_week' NO existe" -ForegroundColor Red
        $needsDayOfWeekMigration = $true
    }
} else {
    Write-Host "3️⃣  Saltando verificación de 'day_of_week' (la tabla no existe)" -ForegroundColor Gray
    $needsDayOfWeekMigration = $true
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Resumen
$needsMigration = $needsRecurrentMigration -or $needsSubscriptionsTable -or $needsDayOfWeekMigration

if (-not $needsMigration) {
    Write-Host "✅ Todas las migraciones están aplicadas correctamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "💡 Si sigues obteniendo errores, intenta:" -ForegroundColor Yellow
    Write-Host "   1. Reiniciar la aplicación en Railway" -ForegroundColor Gray
    Write-Host "   2. Verificar que el código desplegado esté actualizado" -ForegroundColor Gray
    Write-Host "   3. Revisar los logs de Railway para más detalles" -ForegroundColor Gray
} else {
    Write-Host "⚠️  Se encontraron migraciones pendientes:" -ForegroundColor Yellow
    Write-Host ""
    
    if ($needsRecurrentMigration) {
        Write-Host "   ❌ Falta: Campo 'recurrent' en 'class_schedule_patterns'" -ForegroundColor Red
    }
    if ($needsSubscriptionsTable) {
        Write-Host "   ❌ Falta: Tabla 'class_subscriptions'" -ForegroundColor Red
    }
    if ($needsDayOfWeekMigration) {
        Write-Host "   ❌ Falta: Campo 'day_of_week' en 'class_subscriptions'" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "¿Deseas ejecutar las migraciones faltantes ahora? (S/N)" -ForegroundColor Yellow
    $execute = Read-Host
    
    if ($execute -eq "S" -or $execute -eq "s" -or $execute -eq "Y" -or $execute -eq "y") {
        Write-Host ""
        Write-Host "🚀 Ejecutando migraciones..." -ForegroundColor Cyan
        Write-Host ""
        
        # Ejecutar migración de recurrent
        if ($needsRecurrentMigration) {
            Write-Host "📤 Ejecutando: migration-add-recurrent-field.sql" -ForegroundColor Cyan
            $sqlFile = "src/main/resources/sql/migration-add-recurrent-field.sql"
            if (Test-Path $sqlFile) {
                $result = & psql -h $host -p $port -U $username -d $database -f $sqlFile 2>&1
                Write-Host $result
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "✅ Migración ejecutada exitosamente!" -ForegroundColor Green
                } else {
                    Write-Host "❌ Error al ejecutar la migración" -ForegroundColor Red
                }
            } else {
                Write-Host "❌ No se encontró el archivo: $sqlFile" -ForegroundColor Red
            }
            Write-Host ""
        }
        
        # Ejecutar migración de class_subscriptions
        if ($needsSubscriptionsTable) {
            Write-Host "📤 Ejecutando: migration-add-class-subscriptions.sql" -ForegroundColor Cyan
            $sqlFile = "src/main/resources/sql/migration-add-class-subscriptions.sql"
            if (Test-Path $sqlFile) {
                $result = & psql -h $host -p $port -U $username -d $database -f $sqlFile 2>&1
                Write-Host $result
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "✅ Migración ejecutada exitosamente!" -ForegroundColor Green
                } else {
                    Write-Host "❌ Error al ejecutar la migración" -ForegroundColor Red
                }
            } else {
                Write-Host "❌ No se encontró el archivo: $sqlFile" -ForegroundColor Red
            }
            Write-Host ""
        }
        
        # Ejecutar migración de day_of_week
        if ($needsDayOfWeekMigration) {
            Write-Host "📤 Ejecutando: migration-add-day-of-week-to-subscriptions.sql" -ForegroundColor Cyan
            $sqlFile = "src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql"
            if (Test-Path $sqlFile) {
                $result = & psql -h $host -p $port -U $username -d $database -f $sqlFile 2>&1
                Write-Host $result
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "✅ Migración ejecutada exitosamente!" -ForegroundColor Green
                } else {
                    Write-Host "❌ Error al ejecutar la migración" -ForegroundColor Red
                }
            } else {
                Write-Host "❌ No se encontró el archivo: $sqlFile" -ForegroundColor Red
            }
            Write-Host ""
        }
        
        Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "✅ Migraciones ejecutadas. Verifica nuevamente ejecutando este script." -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "⏹️  Migraciones no ejecutadas. Puedes ejecutarlas manualmente más tarde." -ForegroundColor Yellow
    }
}

# Limpiar variable de entorno
$env:PGPASSWORD = $null
$passwordPlain = $null

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan

