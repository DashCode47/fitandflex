# Guía para Aplicar Migración - Campo Recurrent

Esta guía explica cómo aplicar la migración que agrega el campo `recurrent` a la tabla `class_schedule_patterns`.

## 📋 Información de la Migración

- **Archivo:** `src/main/resources/sql/migration-add-recurrent-field.sql`
- **Descripción:** Agrega el campo `recurrent` (BOOLEAN) a la tabla `class_schedule_patterns`
- **Base de datos:** PostgreSQL (Render)

---

## 🔧 Métodos para Aplicar la Migración

### Método 1: Usando psql (Línea de Comandos) - RECOMENDADO

#### En Windows (PowerShell o CMD):

```powershell
# 1. Conectarse a la base de datos usando psql
# Si tienes PostgreSQL instalado localmente:
psql -h dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com -p 5432 -U fitandflex_prod_user -d fitandflex_prod_j1te

# 2. Cuando te pida la contraseña, ingresa: bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T

# 3. Una vez conectado, ejecuta el script:
\i src/main/resources/sql/migration-add-recurrent-field.sql

# O copia y pega el contenido del archivo directamente en psql
```

#### Alternativa: Ejecutar directamente desde PowerShell

```powershell
# Navegar a la carpeta del proyecto
cd C:\Users\david\Documents\Projects\fitandflex

# Ejecutar el script SQL directamente
psql -h dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com -p 5432 -U fitandflex_prod_user -d fitandflex_prod_j1te -f src/main/resources/sql/migration-add-recurrent-field.sql
```

**Nota:** Te pedirá la contraseña. Ingresa: `bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T`

---

### Método 2: Usando pgAdmin (Interfaz Gráfica)

1. **Abrir pgAdmin**
   - Descarga desde: https://www.pgadmin.org/download/

2. **Conectar a la base de datos:**
   - Click derecho en "Servers" → "Create" → "Server"
   - **General Tab:**
     - Name: `FitAndFlex Production`
   - **Connection Tab:**
     - Host: `dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com`
     - Port: `5432`
     - Database: `fitandflex_prod_j1te`
     - Username: `fitandflex_prod_user`
     - Password: `bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T`
     - ✅ Save password

3. **Ejecutar el script:**
   - Click derecho en la base de datos `fitandflex_prod_j1te`
   - Selecciona "Query Tool"
   - Abre el archivo `src/main/resources/sql/migration-add-recurrent-field.sql`
   - Click en el botón "Execute" (⚡) o presiona F5

---

### Método 3: Usando DBeaver (Interfaz Gráfica)

1. **Abrir DBeaver**
   - Descarga desde: https://dbeaver.io/download/

2. **Crear nueva conexión:**
   - Click en "New Database Connection"
   - Selecciona "PostgreSQL"
   - **Configuración:**
     - Host: `dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com`
     - Port: `5432`
     - Database: `fitandflex_prod_j1te`
     - Username: `fitandflex_prod_user`
     - Password: `bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T`
   - Click "Test Connection" y luego "Finish"

3. **Ejecutar el script:**
   - Click derecho en la conexión → "SQL Editor" → "New SQL Script"
   - Abre el archivo `src/main/resources/sql/migration-add-recurrent-field.sql`
   - Click en "Execute SQL Script" (Ctrl+Alt+X)

---

### Método 4: Usando Docker (Si tienes Docker instalado)

```bash
# Ejecutar psql desde un contenedor Docker
docker run -it --rm postgres:15 psql -h dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com -p 5432 -U fitandflex_prod_user -d fitandflex_prod_j1te -f /dev/stdin < src/main/resources/sql/migration-add-recurrent-field.sql
```

---

### Método 5: Desde Render Dashboard (Web)

1. **Acceder a Render Dashboard:**
   - Ve a: https://dashboard.render.com
   - Inicia sesión con tu cuenta

2. **Acceder a la base de datos:**
   - Ve a tu base de datos PostgreSQL
   - Click en "Connect" o "Info"
   - Busca la opción "Connect via psql" o "Query"

3. **Ejecutar el script:**
   - Copia el contenido de `migration-add-recurrent-field.sql`
   - Pégalo en la consola SQL de Render
   - Ejecuta el script

---

## ✅ Verificar que la Migración se Aplicó Correctamente

Después de ejecutar la migración, verifica que el campo se agregó correctamente:

```sql
-- Verificar que la columna existe
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'class_schedule_patterns'
AND column_name = 'recurrent';

-- Deberías ver:
-- column_name: recurrent
-- data_type: boolean
-- column_default: false
-- is_nullable: NO
```

O simplemente:

```sql
-- Ver estructura de la tabla
\d class_schedule_patterns

-- O en SQL estándar:
SELECT * FROM information_schema.columns 
WHERE table_name = 'class_schedule_patterns';
```

---

## 🔍 Qué Hace la Migración

La migración realiza lo siguiente:

1. ✅ **Crea la tabla** `class_schedule_patterns` si no existe
2. ✅ **Crea los índices** necesarios si no existen
3. ✅ **Agrega el campo `recurrent`** (BOOLEAN, NOT NULL, DEFAULT FALSE) si no existe
4. ✅ **Crea un índice** para el campo `recurrent`
5. ✅ **Crea el trigger** para `updated_at` si no existe

**Importante:** El script es **idempotente**, lo que significa que puedes ejecutarlo múltiples veces sin causar errores. Si el campo ya existe, simplemente mostrará un mensaje informativo.

---

## 🚨 Solución de Problemas

### Error: "psql: command not found"

**Solución:** Instala PostgreSQL Client Tools:
- **Windows:** Descarga desde https://www.postgresql.org/download/windows/
- O usa uno de los métodos gráficos (pgAdmin, DBeaver)

### Error: "password authentication failed"

**Solución:** Verifica que las credenciales sean correctas:
- Username: `fitandflex_prod_user`
- Password: `bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T`

### Error: "could not connect to server"

**Solución:** 
- Verifica tu conexión a internet
- Verifica que la IP de Render no haya cambiado
- Verifica que el firewall no esté bloqueando la conexión

### Error: "relation already exists"

**Solución:** Este es normal si la tabla ya existe. El script usa `CREATE TABLE IF NOT EXISTS`, así que no debería causar problemas.

---

## 📝 Comandos Rápidos (Copy & Paste)

### Windows PowerShell:

```powershell
# Conectar y ejecutar migración
$env:PGPASSWORD='bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T'
psql -h dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com -p 5432 -U fitandflex_prod_user -d fitandflex_prod_j1te -f src/main/resources/sql/migration-add-recurrent-field.sql
```

### Linux/Mac:

```bash
# Conectar y ejecutar migración
PGPASSWORD='bR3BHo0UDVlLbZVeYWy27LkEcep7ut5T' psql -h dpg-d46dejfdiees739pcnvg-a.oregon-postgres.render.com -p 5432 -U fitandflex_prod_user -d fitandflex_prod_j1te -f src/main/resources/sql/migration-add-recurrent-field.sql
```

---

## 🎯 Recomendación

**Para Windows, recomiendo usar DBeaver o pgAdmin** ya que son más fáciles de usar y tienen interfaces gráficas intuitivas.

Si prefieres línea de comandos, asegúrate de tener PostgreSQL Client Tools instalado.

---

**¿Necesitas ayuda?** Si encuentras algún problema, verifica:
1. Que tengas acceso a internet
2. Que las credenciales sean correctas
3. Que el archivo SQL esté en la ruta correcta

