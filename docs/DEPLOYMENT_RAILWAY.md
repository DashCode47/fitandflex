# 🚂 Guía Completa: Deployment en Railway - Fit & Flex API

## 🎯 ¿Por qué Railway?

Railway es **la mejor opción** para desplegar aplicaciones Spring Boot porque:
- ✅ Soporte nativo para Java/Spring Boot
- ✅ PostgreSQL incluido con un click
- ✅ Deploy automático desde GitHub
- ✅ SSL/HTTPS automático y gratuito
- ✅ Variables de entorno fáciles de configurar
- ✅ Plan gratuito generoso ($5 créditos/mes)
- ✅ Logs en tiempo real
- ✅ Rollback fácil

---

## 📋 Paso 1: Preparar el Proyecto

### ✅ Verificar Archivos Necesarios

Asegúrate de que tu proyecto tenga estos archivos (ya están creados):

- ✅ `build.gradle` - Configuración de Gradle
- ✅ `application-prod.properties` - Configuración de producción
- ✅ `railway.json` - Configuración de Railway
- ✅ `Procfile` - Comando de inicio
- ✅ `.railwayignore` - Archivos a ignorar

### ✅ Verificar Configuración

Tu `application-prod.properties` debe usar variables de entorno:
- `server.port=${PORT:${SERVER_PORT:8080}}` ✅ (ya configurado)
- `spring.datasource.url=${DATABASE_URL}` ✅
- `app.jwt.secret=${APP_JWT_SECRET}` ✅

---

## 📋 Paso 2: Crear Cuenta en Railway

1. Ve a **https://railway.app**
2. Click en **"Start a New Project"** o **"Login"**
3. Inicia sesión con tu cuenta de **GitHub**
   - Railway necesita acceso a GitHub para deploy automático

---

## 📋 Paso 3: Crear Nuevo Proyecto

1. En el dashboard de Railway, click en **"New Project"**
2. Selecciona **"Deploy from GitHub repo"**
3. Si es la primera vez, autoriza Railway a acceder a tus repositorios
4. Selecciona tu repositorio `fitandflex`
5. Railway detectará automáticamente que es un proyecto Gradle

**Railway automáticamente:**
- Detecta que es un proyecto Java/Gradle
- Configura el build command
- Configura el start command
- Inicia el primer deploy

---

## 📋 Paso 4: Agregar Base de Datos PostgreSQL

1. En tu proyecto Railway, verás tu servicio de aplicación
2. Click en **"+ New"** (botón verde en la parte superior)
3. Selecciona **"Database"**
4. Click en **"Add PostgreSQL"**
5. Railway creará automáticamente una base de datos PostgreSQL

**Railway automáticamente:**
- Crea la base de datos PostgreSQL
- Genera las variables de entorno:
  - `DATABASE_URL`
  - `PGHOST`
  - `PGPORT`
  - `PGUSER`
  - `PGPASSWORD`
  - `PGDATABASE`

---

## 📋 Paso 5: Configurar Variables de Entorno

### 5.1 Variables de la Base de Datos

Railway genera automáticamente las variables de PostgreSQL, pero Spring Boot necesita el formato JDBC.

1. Ve a tu servicio de **aplicación** (no el de PostgreSQL)
2. Click en la pestaña **"Variables"**
3. Railway debería haber generado automáticamente:
   - `DATABASE_URL` (formato JDBC)
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`

**Si no están en formato JDBC**, agrega manualmente:

```bash
DATABASE_URL=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}
DATABASE_USERNAME=${PGUSER}
DATABASE_PASSWORD=${PGPASSWORD}
```

### 5.2 Variables de la Aplicación

Agrega estas variables en tu servicio de aplicación:

```bash
# Application
SPRING_PROFILES_ACTIVE=prod
PORT=8080

# Database Pool (opcional, pero recomendado)
DB_POOL_SIZE=20
DB_MIN_IDLE=5

# JWT Security (CRÍTICO: Genera secretos seguros)
APP_JWT_SECRET=GENERA_UN_SECRETO_SEGURO_DE_64_CARACTERES_AQUI
APP_JWT_EXPIRATION=86400000
JWT_SECRET=GENERA_OTRO_SECRETO_SEGURO_DE_64_CARACTERES_AQUI
JWT_EXPIRATION=86400000

# CORS (Actualizar con tu dominio de Railway después del deploy)
CORS_ALLOWED_ORIGINS=https://tu-proyecto.railway.app,https://tu-frontend.com

# Logging
LOG_LEVEL=INFO
SECURITY_LOG_LEVEL=WARN
HIBERNATE_LOG_LEVEL=WARN
SHOW_SQL=false
```

### 5.3 Generar Secretos JWT Seguros

**Opción 1: OpenSSL (Recomendado)**
```bash
# Windows (PowerShell)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))

# Linux/Mac
openssl rand -base64 64
```

**Opción 2: Online Generator**
- Ve a: https://www.grc.com/passwords.htm
- Genera una contraseña de 64 caracteres
- Úsala como `APP_JWT_SECRET` y `JWT_SECRET`

**Opción 3: Java**
```java
import java.security.SecureRandom;
import java.util.Base64;

SecureRandom random = new SecureRandom();
byte[] bytes = new byte[64];
random.nextBytes(bytes);
String secret = Base64.getEncoder().encodeToString(bytes);
System.out.println(secret);
```

---

## 📋 Paso 6: Configurar Build Settings (Opcional)

Railway detecta automáticamente Gradle, pero puedes verificar:

1. Ve a tu servicio de aplicación
2. Click en **"Settings"** → **"Build & Deploy"**
3. Verifica:
   - **Build Command**: `./gradlew build -x test`
   - **Start Command**: `java -jar build/libs/fitandflex-0.0.1-SNAPSHOT.jar`
   - **Watch Paths**: `src/**`

---

## 📋 Paso 7: Deploy

### Deploy Automático

Railway desplegará automáticamente cuando:
- Haces push a tu repositorio GitHub
- Cambias variables de entorno
- Haces click en **"Redeploy"**

### Deploy Manual

1. En tu servicio, click en **"Deploy"**
2. Espera a que el build termine (3-5 minutos la primera vez)
3. Railway te dará una URL: `https://tu-proyecto.railway.app`

### Monitorear el Deploy

1. Ve a la pestaña **"Deployments"**
2. Click en el deployment más reciente
3. Click en **"View Logs"** para ver los logs en tiempo real

---

## 📋 Paso 8: Verificar Deployment

### 8.1 Health Check

```bash
curl https://tu-proyecto.railway.app/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

### 8.2 Swagger UI

Abre en tu navegador:
```
https://tu-proyecto.railway.app/swagger-ui.html
```

### 8.3 Probar Login

```bash
curl -X POST https://tu-proyecto.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fitandflex.com","password":"admin123"}'
```

### 8.4 Ver Logs

1. En Railway, ve a tu servicio
2. Click en **"View Logs"**
3. Verás los logs en tiempo real

---

## 📋 Paso 9: Actualizar CORS

Después de obtener tu URL de Railway:

1. Ve a **Variables** en tu servicio
2. Actualiza `CORS_ALLOWED_ORIGINS`:
   ```bash
   CORS_ALLOWED_ORIGINS=https://tu-proyecto.railway.app,https://tu-frontend.com
   ```
3. Railway hará un redeploy automático

---

## 📋 Paso 10: Configurar Dominio Personalizado (Opcional)

1. En Railway, ve a tu servicio
2. Click en **"Settings"** → **"Networking"**
3. Click en **"Generate Domain"** para obtener un dominio `.railway.app`
4. O agrega tu dominio personalizado:
   - Click en **"Custom Domain"**
   - Ingresa tu dominio
   - Configura los DNS según las instrucciones

---

## 🔧 Configuración Adicional

### Variables de Entorno de PostgreSQL

Railway genera automáticamente estas variables para PostgreSQL:
- `DATABASE_URL` - URL completa de conexión (formato JDBC)
- `PGHOST` - Host de PostgreSQL
- `PGPORT` - Puerto
- `PGUSER` - Usuario
- `PGPASSWORD` - Contraseña
- `PGDATABASE` - Nombre de la base de datos

**Nota**: Si `DATABASE_URL` no está en formato JDBC, puedes construirlo así en las variables de entorno:

```bash
DATABASE_URL=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}
DATABASE_USERNAME=${PGUSER}
DATABASE_PASSWORD=${PGPASSWORD}
```

### Health Check

Railway verifica automáticamente el endpoint `/actuator/health`. Asegúrate de que esté habilitado en tu `application-prod.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

---

## 🔍 Troubleshooting

### Error: "Port already in use"
- **Solución**: Railway asigna el puerto automáticamente. Usa `${PORT}` en lugar de un puerto fijo.

### Error: "Database connection failed"
- **Solución**: 
  1. Verifica que las variables de entorno de PostgreSQL estén configuradas
  2. Asegúrate de que el servicio PostgreSQL esté corriendo
  3. Verifica el formato de `DATABASE_URL` (debe ser JDBC)

### Error: "Build failed"
- **Solución**: 
  1. Revisa los logs en Railway
  2. Verifica que `build.gradle` esté correcto
  3. Asegúrate de que Java 17 esté disponible

### La aplicación no inicia
- **Solución**: 
  1. Revisa los logs en tiempo real
  2. Verifica que todas las variables de entorno estén configuradas
  3. Asegúrate de que el perfil `prod` esté activo (`SPRING_PROFILES_ACTIVE=prod`)

### Error: "JWT secret is empty"
- **Solución**: 
  1. Verifica que `APP_JWT_SECRET` y `JWT_SECRET` estén configurados
  2. Genera secretos seguros de al menos 64 caracteres

### Error: "CORS policy blocked"
- **Solución**: 
  1. Actualiza `CORS_ALLOWED_ORIGINS` con tu dominio de Railway
  2. Haz un redeploy después de cambiar las variables

---

## 📊 Monitoreo

### Logs en Tiempo Real

1. Ve a tu servicio en Railway
2. Click en **"View Logs"**
3. Verás los logs en tiempo real

### Métricas

Railway muestra automáticamente:
- CPU usage
- Memory usage
- Network traffic

### Health Checks

Railway verifica automáticamente `/actuator/health` cada 30 segundos.

---

## 💰 Costos

- **Plan Gratuito**: $5 créditos/mes (suficiente para desarrollo)
- **Pro Plan**: $20/mes (para producción)
- PostgreSQL incluido en ambos planes

**Nota**: El plan gratuito es suficiente para desarrollo y pruebas.

---

## 🔗 Enlaces Útiles

- Railway Dashboard: https://railway.app/dashboard
- Railway Docs: https://docs.railway.app
- Railway Discord: https://discord.gg/railway

---

## ✅ Checklist de Deployment

- [ ] Cuenta de Railway creada
- [ ] Proyecto conectado a GitHub
- [ ] Servicio PostgreSQL agregado
- [ ] Variables de entorno configuradas:
  - [ ] `SPRING_PROFILES_ACTIVE=prod`
  - [ ] `DATABASE_URL` (formato JDBC)
  - [ ] `APP_JWT_SECRET` (generado)
  - [ ] `JWT_SECRET` (generado)
  - [ ] `CORS_ALLOWED_ORIGINS` (con tu dominio)
- [ ] Build settings verificados
- [ ] Deploy exitoso
- [ ] Health check funcionando (`/actuator/health`)
- [ ] API respondiendo correctamente
- [ ] Swagger UI accesible
- [ ] CORS configurado correctamente
- [ ] Logs verificados

---

## 🎉 ¡Listo!

Tu API está desplegada en Railway. Railway:
- ✅ Maneja SSL/HTTPS automáticamente
- ✅ Reinicia automáticamente si falla
- ✅ Muestra logs en tiempo real
- ✅ Permite rollback fácil
- ✅ Deploy automático desde GitHub

---

*Última actualización: Diciembre 2024*

