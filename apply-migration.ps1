# ===========================================
# Script para aplicar migración de base de datos
# ===========================================
# Este script aplica la migración que agrega el campo 'recurrent'
# a la tabla class_schedule_patterns

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Aplicando migración: Campo Recurrent" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# Configuración de la base de datos
$DB_HOST = "dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com"
$DB_PORT = "5432"
$DB_NAME = "fitandflex_prod_j1te"
$DB_USER = "fitandflex_prod_user"
$DB_PASSWORD = "bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T"
$MIGRATION_FILE = "src/main/resources/sql/migration-add-recurrent-field.sql"

# Verificar que el archivo de migración existe
if (-not (Test-Path $MIGRATION_FILE)) {
    Write-Host "ERROR: No se encontró el archivo de migración: $MIGRATION_FILE" -ForegroundColor Red
    Write-Host "Asegúrate de ejecutar este script desde la raíz del proyecto." -ForegroundColor Yellow
    exit 1
}

Write-Host "Archivo de migración encontrado: $MIGRATION_FILE" -ForegroundColor Green
Write-Host ""

# Verificar si psql está instalado
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psqlPath) {
    Write-Host "ERROR: psql no está instalado o no está en el PATH." -ForegroundColor Red
    Write-Host ""
    Write-Host "Opciones:" -ForegroundColor Yellow
    Write-Host "1. Instala PostgreSQL Client Tools desde: https://www.postgresql.org/download/windows/" -ForegroundColor Yellow
    Write-Host "2. O usa DBeaver/pgAdmin para ejecutar el script manualmente" -ForegroundColor Yellow
    Write-Host "3. Ver la guía completa en: docs/MIGRATION_GUIDE.md" -ForegroundColor Yellow
    exit 1
}

Write-Host "psql encontrado: $($psqlPath.Source)" -ForegroundColor Green
Write-Host ""

# Establecer la variable de entorno para la contraseña
$env:PGPASSWORD = $DB_PASSWORD

Write-Host "Conectando a la base de datos..." -ForegroundColor Cyan
Write-Host "Host: $DB_HOST" -ForegroundColor Gray
Write-Host "Database: $DB_NAME" -ForegroundColor Gray
Write-Host "User: $DB_USER" -ForegroundColor Gray
Write-Host ""

try {
    # Ejecutar el script de migración
    Write-Host "Ejecutando migración..." -ForegroundColor Cyan
    Write-Host ""
    
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $MIGRATION_FILE
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "===========================================" -ForegroundColor Green
        Write-Host "✓ Migración aplicada exitosamente!" -ForegroundColor Green
        Write-Host "===========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "El campo 'recurrent' ha sido agregado a la tabla 'class_schedule_patterns'." -ForegroundColor Green
        Write-Host ""
        Write-Host "Para verificar, ejecuta:" -ForegroundColor Yellow
        Write-Host "  psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c `"SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'class_schedule_patterns' AND column_name = 'recurrent';`"" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "===========================================" -ForegroundColor Red
        Write-Host "✗ Error al aplicar la migración" -ForegroundColor Red
        Write-Host "===========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Revisa los mensajes de error arriba." -ForegroundColor Yellow
        Write-Host "Verifica:" -ForegroundColor Yellow
        Write-Host "  - Que tengas conexión a internet" -ForegroundColor Yellow
        Write-Host "  - Que las credenciales sean correctas" -ForegroundColor Yellow
        Write-Host "  - Que la base de datos esté accesible" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    # Limpiar la variable de entorno de la contraseña
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}

