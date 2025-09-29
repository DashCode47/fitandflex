# 🚀 Guía de Deployment - Fit & Flex API

Esta guía te ayudará a exponer tu backend Spring Boot con una API externa de manera segura y escalable.

## 📋 Prerrequisitos

- Java 17 o superior
- Docker y Docker Compose
- PostgreSQL (o usar el contenedor incluido)
- Certificados SSL (para producción)

## 🔧 Configuración Inicial

### 1. Variables de Entorno

Copia el archivo `env.example` a `.env` y configura las variables:

```bash
cp env.example .env
```

Edita el archivo `.env` con tus valores:

```env
# Base de datos
DATABASE_PASSWORD=tu_password_seguro_aqui
DATABASE_URL=jdbc:postgresql://localhost:5432/fitandflex_prod

# JWT Secrets (¡Muy importantes!)
JWT_SECRET=tu_jwt_secret_muy_largo_y_seguro_aqui
APP_JWT_SECRET=tu_app_jwt_secret_muy_largo_y_seguro_aqui

# CORS (dominios permitidos)
CORS_ALLOWED_ORIGINS=https://tudominio.com,https://api.tudominio.com

# SSL (para producción)
SSL_ENABLED=true
SSL_KEYSTORE=/path/to/your/keystore.p12
SSL_KEYSTORE_PASSWORD=tu_password_keystore
```

### 2. Generar Certificados SSL (Producción)

Para producción, necesitas certificados SSL válidos:

```bash
# Opción 1: Let's Encrypt (gratuito)
sudo apt install certbot
sudo certbot certonly --standalone -d api.tudominio.com

# Opción 2: Certificado autofirmado (solo para desarrollo)
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes
```

## 🐳 Deployment con Docker

### Desarrollo Local

```bash
# Construir y levantar todos los servicios
docker-compose up --build -d

# Ver logs
docker-compose logs -f

# Parar servicios
docker-compose down
```

### Producción

```bash
# Usar el script de deployment
./deploy.sh  # Linux/Mac
# o
.\deploy.ps1  # Windows PowerShell
```

## 🌐 Configuración de Dominio

### 1. Configurar DNS

Apunta tu dominio a la IP del servidor:

```
A    api.tudominio.com    -> IP_DEL_SERVIDOR
```

### 2. Configurar Nginx

El archivo `nginx.conf` ya está configurado para:
- Redirección HTTP → HTTPS
- Rate limiting
- Headers de seguridad
- Proxy reverso

### 3. Actualizar CORS

En tu archivo `.env`, actualiza:

```env
CORS_ALLOWED_ORIGINS=https://tudominio.com,https://www.tudominio.com,https://api.tudominio.com
```

## 🔒 Seguridad

### Headers de Seguridad

La aplicación incluye:
- ✅ CORS configurado
- ✅ Rate limiting
- ✅ Headers de seguridad
- ✅ JWT Authentication
- ✅ HTTPS (en producción)

### Rate Limiting

- **API general**: 10 requests/segundo
- **Autenticación**: 5 requests/segundo
- **Burst**: 20 requests (API), 10 requests (Auth)

## 📊 Monitoreo

### Health Checks

```bash
# Verificar estado de la aplicación
curl http://localhost:8080/actuator/health

# Métricas
curl http://localhost:8080/actuator/metrics
```

### Logs

```bash
# Ver logs de la aplicación
docker-compose logs -f app

# Ver logs de todos los servicios
docker-compose logs -f
```

## 📚 Documentación de la API

Una vez desplegada, la documentación estará disponible en:

- **Swagger UI**: `http://tu-dominio/swagger-ui.html`
- **OpenAPI JSON**: `http://tu-dominio/v3/api-docs`

## 🚀 Endpoints Principales

### Autenticación
```
POST /api/auth/login
```

### API Protegida
```
GET /api/branches
POST /api/branches
GET /api/classes
POST /api/classes
```

## 🔧 Troubleshooting

### Problema: "Rate limit exceeded"
- **Solución**: Ajusta los límites en `nginx.conf` o `RateLimitingFilter.java`

### Problema: "CORS error"
- **Solución**: Verifica `CORS_ALLOWED_ORIGINS` en `.env`

### Problema: "Database connection failed"
- **Solución**: Verifica las variables de base de datos en `.env`

### Problema: "SSL certificate error"
- **Solución**: Verifica que los certificados estén en `/ssl/` y las rutas sean correctas

## 📈 Escalabilidad

### Para mayor tráfico:

1. **Aumentar recursos del contenedor**:
```yaml
# En docker-compose.yml
app:
  deploy:
    resources:
      limits:
        memory: 1G
        cpus: '0.5'
```

2. **Usar Redis para sesiones**:
```yaml
# Ya incluido en docker-compose.yml
redis:
  image: redis:7-alpine
```

3. **Load Balancer**:
```yaml
# Agregar múltiples instancias
app1:
  # ... configuración
app2:
  # ... configuración
```

## 🆘 Soporte

Si tienes problemas:

1. Revisa los logs: `docker-compose logs -f`
2. Verifica la configuración: `docker-compose config`
3. Reinicia los servicios: `docker-compose restart`

## 📝 Checklist de Deployment

- [ ] Variables de entorno configuradas
- [ ] Certificados SSL instalados
- [ ] Dominio configurado en DNS
- [ ] CORS configurado correctamente
- [ ] Rate limiting ajustado
- [ ] Health checks funcionando
- [ ] Logs monitoreados
- [ ] Backup de base de datos configurado

¡Tu API está lista para ser expuesta al mundo! 🌍
