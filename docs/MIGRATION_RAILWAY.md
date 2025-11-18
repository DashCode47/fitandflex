# 🚂 Guía: Ejecutar Migración en Railway

Esta guía explica cómo ejecutar la migración `migration-add-day-of-week-to-subscriptions.sql` en tu base de datos de Railway.

---

## 📋 Paso 1: Obtener Credenciales de Railway

### Opción A: Desde Railway Dashboard (Recomendado)

1. **Accede a Railway Dashboard:**
   - Ve a: https://railway.app
   - Inicia sesión con tu cuenta

2. **Selecciona tu proyecto:**
   - Click en el proyecto `fitandflex`

3. **Accede a la base de datos PostgreSQL:**
   - Click en el servicio **PostgreSQL** (o el nombre que le hayas dado)
   - Ve a la pestaña **"Variables"**

4. **Copia las credenciales:**
   - `PGHOST` → Host de la base de datos
   - `PGPORT` → Puerto (generalmente `5432`)
   - `PGUSER` → Usuario
   - `PGPASSWORD` → Contraseña
   - `PGDATABASE` → Nombre de la base de datos

### Opción B: Desde Variables de Entorno de la Aplicación

1. En Railway Dashboard, ve a tu servicio de **aplicación** (no PostgreSQL)
2. Ve a la pestaña **"Variables"**
3. Busca `DATABASE_URL` o las variables `PG*`
4. Si `DATABASE_URL` está en formato JDBC, extrae los valores:
   ```
   jdbc:postgresql://HOST:PORT/DATABASE
   ```

---

## 🔧 Método 1: Usando Railway CLI (Más Fácil)

### Instalar Railway CLI

**Windows (PowerShell):**
```powershell
# Instalar Railway CLI
iwr https://railway.app/install.ps1 | iex
```

**Mac/Linux:**
```bash
curl -fsSL https://railway.app/install.sh | sh
```

### Ejecutar Migración

1. **Iniciar sesión:**
```bash
railway login
```

2. **Conectar al proyecto:**
```bash
railway link
# Selecciona tu proyecto fitandflex
```

3. **Conectarse a PostgreSQL:**
```bash
railway connect postgres
```

4. **Ejecutar el script SQL:**
```bash
# Desde Railway CLI, ejecuta:
\i src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql
```

O copia y pega el contenido del archivo directamente en la consola de PostgreSQL.

---

## 🔧 Método 2: Usando psql desde tu Computadora

### Paso 1: Obtener Credenciales

Desde Railway Dashboard, copia estos valores de las variables de entorno:

```
PGHOST=containers-us-west-XXX.railway.app
PGPORT=5432
PGUSER=postgres
PGPASSWORD=tu_password_aqui
PGDATABASE=railway
```

### Paso 2: Ejecutar Migración

**Windows (PowerShell):**
```powershell
# Navegar al proyecto
cd C:\Users\david\Documents\Projects\fitandflex

# Opción 1: Usar variable de entorno para la contraseña
$env:PGPASSWORD='tu_password_aqui'
psql -h containers-us-west-XXX.railway.app -p 5432 -U postgres -d railway -f src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql

# Opción 2: Ejecutar directamente (te pedirá la contraseña)
psql -h containers-us-west-XXX.railway.app -p 5432 -U postgres -d railway -f src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql
```

**Linux/Mac:**
```bash
# Navegar al proyecto
cd ~/Projects/fitandflex

# Opción 1: Usar variable de entorno para la contraseña
PGPASSWORD='tu_password_aqui' psql -h containers-us-west-XXX.railway.app -p 5432 -U postgres -d railway -f src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql

# Opción 2: Ejecutar directamente (te pedirá la contraseña)
psql -h containers-us-west-XXX.railway.app -p 5432 -U postgres -d railway -f src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql
```

**Nota:** Reemplaza `containers-us-west-XXX.railway.app` y `tu_password_aqui` con los valores reales de Railway.

---

## 🔧 Método 3: Usando DBeaver (Interfaz Gráfica)

### Paso 1: Crear Conexión

1. **Abrir DBeaver**
   - Si no lo tienes: https://dbeaver.io/download/

2. **Crear nueva conexión:**
   - Click en "New Database Connection" (🔌)
   - Selecciona **"PostgreSQL"**
   - Click "Next"

3. **Configurar conexión:**
   - **Host:** `containers-us-west-XXX.railway.app` (de `PGHOST`)
   - **Port:** `5432` (de `PGPORT`)
   - **Database:** `railway` (de `PGDATABASE`)
   - **Username:** `postgres` (de `PGUSER`)
   - **Password:** `tu_password_aqui` (de `PGPASSWORD`)
   - ✅ **Save password**

4. **Probar conexión:**
   - Click "Test Connection"
   - Si pide descargar driver, acepta
   - Debería mostrar "Connected"

5. **Guardar:**
   - Click "Finish"

### Paso 2: Ejecutar Migración

1. **Abrir SQL Editor:**
   - Click derecho en la conexión → "SQL Editor" → "New SQL Script"

2. **Abrir el archivo SQL:**
   - File → Open File
   - Selecciona: `src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql`

3. **Ejecutar script:**
   - Click en "Execute SQL Script" (Ctrl+Alt+X) o el botón ▶️
   - O selecciona todo el contenido (Ctrl+A) y ejecuta (F5)

4. **Verificar resultado:**
   - Deberías ver mensajes de éxito:
     ```
     ALTER TABLE
     UPDATE X
     CREATE INDEX
     ...
     ```

---

## 🔧 Método 4: Usando Railway Dashboard (Query Tool)

Railway tiene un Query Tool integrado:

1. **Accede a Railway Dashboard:**
   - Ve a tu proyecto en https://railway.app

2. **Abre PostgreSQL:**
   - Click en el servicio PostgreSQL
   - Ve a la pestaña **"Query"** o **"Data"**

3. **Ejecutar SQL:**
   - Abre el archivo `migration-add-day-of-week-to-subscriptions.sql`
   - Copia todo el contenido
   - Pégalo en el Query Tool
   - Click "Run" o presiona Ctrl+Enter

---

## 🔧 Método 5: Usando Script PowerShell (Automático)

Crea un archivo `apply-migration-railway.ps1`:

```powershell
# Script para ejecutar migración en Railway
# Uso: .\apply-migration-railway.ps1

Write-Host "🚂 Ejecutando migración en Railway..." -ForegroundColor Cyan

# Solicitar credenciales
$host = Read-Host "Ingresa PGHOST (ej: containers-us-west-XXX.railway.app)"
$port = Read-Host "Ingresa PGPORT (default: 5432)" 
if ([string]::IsNullOrWhiteSpace($port)) { $port = "5432" }
$database = Read-Host "Ingresa PGDATABASE (ej: railway)"
$username = Read-Host "Ingresa PGUSER (ej: postgres)"
$password = Read-Host "Ingresa PGPASSWORD" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

# Archivo SQL
$sqlFile = "src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ Error: No se encontró el archivo $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "📄 Archivo SQL encontrado: $sqlFile" -ForegroundColor Green

# Ejecutar migración
try {
    $env:PGPASSWORD = $passwordPlain
    psql -h $host -p $port -U $username -d $database -f $sqlFile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Migración ejecutada exitosamente!" -ForegroundColor Green
    } else {
        Write-Host "❌ Error al ejecutar la migración" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
} finally {
    $env:PGPASSWORD = $null
}
```

**Ejecutar:**
```powershell
.\apply-migration-railway.ps1
```

---

## ✅ Verificar Migración Exitosa

Después de ejecutar la migración, verifica que se aplicó correctamente:

```sql
-- Verificar que la columna existe
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'class_subscriptions' 
AND column_name = 'day_of_week';

-- Verificar que los datos se actualizaron
SELECT day_of_week, COUNT(*) 
FROM class_subscriptions 
GROUP BY day_of_week;

-- Verificar índices
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'class_subscriptions' 
AND indexname LIKE '%day%';
```

---

## 🐛 Troubleshooting

### Error: "could not connect to server"

**Solución:**
- Verifica que `PGHOST` sea correcto
- Verifica tu conexión a internet
- Railway puede requerir que tu IP esté en la whitelist (verifica en Railway Dashboard)

### Error: "password authentication failed"

**Solución:**
- Verifica que `PGPASSWORD` sea correcto
- Copia la contraseña directamente desde Railway Dashboard (puede tener caracteres especiales)

### Error: "relation class_subscriptions does not exist"

**Solución:**
- Primero ejecuta la migración inicial: `migration-add-class-subscriptions.sql`
- Verifica que estás conectado a la base de datos correcta

### Error: "column day_of_week already exists"

**Solución:**
- La migración ya se ejecutó anteriormente
- Esto es normal, el script usa `IF NOT EXISTS` para evitar errores

---

## 📝 Comandos Rápidos (Copy & Paste)

### Windows PowerShell (con credenciales de Railway):

```powershell
# Reemplaza estos valores con los de Railway:
$env:PGPASSWORD='TU_PASSWORD_AQUI'
psql -h TU_HOST_AQUI -p 5432 -U postgres -d railway -f src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql
```

### Linux/Mac:

```bash
PGPASSWORD='TU_PASSWORD_AQUI' psql -h TU_HOST_AQUI -p 5432 -U postgres -d railway -f src/main/resources/sql/migration-add-day-of-week-to-subscriptions.sql
```

---

## 🎯 Recomendación

**Para Railway, recomiendo usar:**
1. **DBeaver** (más fácil y visual)
2. **Railway CLI** (si ya lo tienes instalado)
3. **psql** (si prefieres línea de comandos)

---

## 📚 Próximos Pasos

Después de ejecutar la migración:

1. ✅ Verifica que la migración se aplicó correctamente
2. ✅ Reinicia tu aplicación en Railway (si es necesario)
3. ✅ Prueba crear una suscripción con `dayOfWeek`
4. ✅ Verifica que el conteo por día funciona correctamente

---

**¿Necesitas ayuda?** Si encuentras algún problema:
1. Verifica las credenciales en Railway Dashboard
2. Verifica que el archivo SQL esté en la ruta correcta
3. Revisa los logs de Railway para más detalles

