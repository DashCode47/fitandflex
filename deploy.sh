#!/bin/bash

# ===========================================
# FIT & FLEX - DEPLOYMENT SCRIPT
# ===========================================

set -e

echo "🚀 Iniciando deployment de Fit & Flex API..."

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

warn() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

# Verificar que Docker esté instalado
if ! command -v docker &> /dev/null; then
    error "Docker no está instalado. Por favor instala Docker primero."
fi

# Verificar que Docker Compose esté instalado
if ! command -v docker-compose &> /dev/null; then
    error "Docker Compose no está instalado. Por favor instala Docker Compose primero."
fi

# Verificar que el archivo .env existe
if [ ! -f .env ]; then
    error "Archivo .env no encontrado. Por favor crea el archivo .env basado en env.example"
fi

# Cargar variables de entorno
source .env

# Verificar variables críticas
if [ -z "$DATABASE_PASSWORD" ]; then
    error "DATABASE_PASSWORD no está definida en .env"
fi

if [ -z "$JWT_SECRET" ]; then
    error "JWT_SECRET no está definida en .env"
fi

log "Variables de entorno cargadas correctamente"

# Construir la aplicación
log "Construyendo la aplicación..."
./gradlew clean build -x test

# Verificar que el JAR se construyó correctamente
if [ ! -f "build/libs/fitandflex-*.jar" ]; then
    error "El archivo JAR no se construyó correctamente"
fi

log "Aplicación construida exitosamente"

# Parar contenedores existentes
log "Parando contenedores existentes..."
docker-compose down || true

# Limpiar imágenes antiguas
log "Limpiando imágenes antiguas..."
docker system prune -f

# Construir y levantar los servicios
log "Construyendo y levantando servicios..."
docker-compose up --build -d

# Esperar a que los servicios estén listos
log "Esperando a que los servicios estén listos..."
sleep 30

# Verificar que la aplicación esté funcionando
log "Verificando estado de la aplicación..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    log "✅ Aplicación desplegada exitosamente!"
    log "🌐 API disponible en: http://localhost:8080"
    log "📚 Documentación Swagger: http://localhost:8080/swagger-ui.html"
    log "🔍 Health Check: http://localhost:8080/actuator/health"
else
    error "❌ La aplicación no está respondiendo correctamente"
fi

# Mostrar logs de los contenedores
log "Mostrando logs de los contenedores..."
docker-compose logs --tail=50

log "🎉 Deployment completado exitosamente!"
