# ⚡ Quick Start: Deploy en Railway (5 minutos)

## 🚀 Deployment Rápido en Railway

### Paso 1: Crear Cuenta (1 minuto)
1. Ve a https://railway.app
2. Click en **"Start a New Project"**
3. Inicia sesión con **GitHub**

### Paso 2: Conectar Repositorio (1 minuto)
1. Click en **"New Project"**
2. Selecciona **"Deploy from GitHub repo"**
3. Selecciona tu repositorio `fitandflex`

### Paso 3: Agregar PostgreSQL (1 minuto)
1. Click en **"+ New"**
2. Selecciona **"Database"** → **"Add PostgreSQL"**
3. ✅ Railway configura automáticamente las variables de BD

### Paso 4: Configurar Variables (2 minutos)
En tu servicio de aplicación, ve a **"Variables"** y agrega:

```bash
SPRING_PROFILES_ACTIVE=prod
APP_JWT_SECRET=GENERA_UN_SECRETO_SEGURO_DE_64_CARACTERES_AQUI
JWT_SECRET=GENERA_OTRO_SECRETO_SEGURO_DE_64_CARACTERES_AQUI
CORS_ALLOWED_ORIGINS=https://tu-proyecto.railway.app
LOG_LEVEL=INFO
SHOW_SQL=false
```

**Generar secretos:**
```bash
# Windows PowerShell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))

# Linux/Mac
openssl rand -base64 64

# Online
https://www.grc.com/passwords.htm
```

### Paso 5: Deploy Automático ✅
- Railway detecta Gradle automáticamente
- Build y deploy en ~3-5 minutos
- Obtienes URL: `https://tu-proyecto.railway.app`

### Paso 6: Verificar
```bash
# Health check
curl https://tu-proyecto.railway.app/actuator/health

# Swagger UI
https://tu-proyecto.railway.app/swagger-ui.html
```

---

## 🎯 ¡Listo!

Tu API está desplegada. Railway:
- ✅ Maneja SSL/HTTPS automáticamente
- ✅ Reinicia automáticamente si falla
- ✅ Muestra logs en tiempo real
- ✅ Permite rollback fácil

---

## 📚 Documentación Completa

Para más detalles, ve a: [DEPLOYMENT_RAILWAY.md](./DEPLOYMENT_RAILWAY.md)

---

## 🔧 Troubleshooting Rápido

**Error de build?**
- Verifica que `build.gradle` esté correcto
- Revisa logs en Railway

**Error de conexión a BD?**
- Verifica que PostgreSQL esté corriendo
- Revisa variables de entorno de BD (formato JDBC)

**App no inicia?**
- Revisa logs en tiempo real
- Verifica que `SPRING_PROFILES_ACTIVE=prod`

---

*Tiempo total: ~5 minutos* ⚡

