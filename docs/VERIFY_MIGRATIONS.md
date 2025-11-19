# 🔍 Guía: Verificar Migraciones en Railway

Esta guía te ayuda a verificar si las migraciones se ejecutaron correctamente en Railway.

---

## 🚀 Método Rápido: Script de Verificación

Ejecuta el script que verifica y corrige automáticamente:

```powershell
.\verify-and-fix-migrations-railway.ps1
```

Este script:
- ✅ Verifica qué migraciones faltan
- ✅ Te muestra el estado actual
- ✅ Te permite ejecutar las migraciones faltantes automáticamente

---

## 📋 Verificación Manual con SQL

Si prefieres verificar manualmente, puedes usar estos comandos SQL:

### 1. Verificar columna `recurrent` en `class_schedule_patterns`

```sql
-- Verificar si la columna existe
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'class_schedule_patterns'
AND column_name = 'recurrent';
```

**Resultado esperado:**
```
column_name | data_type | column_default | is_nullable
------------|-----------|----------------|-------------
recurrent   | boolean   | false          | NO
```

**Si no existe:** Verás 0 filas o un resultado vacío.

---

### 2. Verificar tabla `class_subscriptions`

```sql
-- Verificar si la tabla existe
SELECT table_name
FROM information_schema.tables
WHERE table_name = 'class_subscriptions';
```

**Resultado esperado:**
```
table_name
-----------
class_subscriptions
```

**Si no existe:** Verás 0 filas.

---

### 3. Verificar columna `day_of_week` en `class_subscriptions`

```sql
-- Verificar si la columna existe
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'class_subscriptions'
AND column_name = 'day_of_week';
```

**Resultado esperado:**
```
column_name | data_type | is_nullable
------------|-----------|-------------
day_of_week | integer   | NO
```

**Si no existe:** Verás 0 filas.

---

## 🔧 Ejecutar Migraciones Manualmente

### Opción 1: Usando psql

```powershell
# Conectar a Railway
$env:PGPASSWORD='TU_PASSWORD'
psql -h TU_HOST -p 5432 -U postgres -d railway

# Dentro de psql, ejecutar:
\i src/main/resources/sql/migration-add-recurrent-field.sql
```

### Opción 2: Ejecutar directamente

```powershell
$env:PGPASSWORD='TU_PASSWORD'
psql -h TU_HOST -p 5432 -U postgres -d railway -f src/main/resources/sql/migration-add-recurrent-field.sql
```

### Opción 3: Usando DBeaver

1. Conéctate a Railway
2. Abre el archivo SQL: `src/main/resources/sql/migration-add-recurrent-field.sql`
3. Ejecuta el script (Ctrl+Alt+X o F5)

---

## 🐛 Solución Rápida: SQL Directo

Si solo necesitas agregar la columna `recurrent` rápidamente, ejecuta este SQL directamente:

```sql
-- Agregar columna recurrent si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'class_schedule_patterns' 
        AND column_name = 'recurrent'
    ) THEN
        ALTER TABLE class_schedule_patterns 
        ADD COLUMN recurrent BOOLEAN NOT NULL DEFAULT FALSE;
        
        CREATE INDEX IF NOT EXISTS idx_schedule_pattern_recurrent 
        ON class_schedule_patterns(recurrent);
        
        RAISE NOTICE 'Campo recurrent agregado exitosamente';
    ELSE
        RAISE NOTICE 'El campo recurrent ya existe';
    END IF;
END $$;
```

---

## ✅ Verificación Completa

Ejecuta este SQL para ver el estado completo de todas las migraciones:

```sql
-- Verificar todas las columnas de class_schedule_patterns
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'class_schedule_patterns'
ORDER BY ordinal_position;

-- Verificar todas las columnas de class_subscriptions (si existe)
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'class_subscriptions'
ORDER BY ordinal_position;
```

---

## 🔍 Troubleshooting

### Error: "column does not exist"

**Causa:** La migración no se ejecutó o falló.

**Solución:**
1. Ejecuta el script de verificación: `.\verify-and-fix-migrations-railway.ps1`
2. O ejecuta la migración manualmente usando los métodos arriba

### Error persiste después de ejecutar migración

**Posibles causas:**
1. **Conexión incorrecta:** Estás conectado a la base de datos incorrecta
2. **Aplicación no reiniciada:** La aplicación en Railway necesita reiniciarse
3. **Código desactualizado:** El código desplegado no coincide con el código local

**Soluciones:**
1. Verifica que estás conectado a la base de datos correcta de Railway
2. Reinicia la aplicación en Railway Dashboard
3. Verifica que el último deploy incluye los cambios del código

---

## 📝 Checklist de Verificación

- [ ] Columna `recurrent` existe en `class_schedule_patterns`
- [ ] Tabla `class_subscriptions` existe
- [ ] Columna `day_of_week` existe en `class_subscriptions`
- [ ] Aplicación reiniciada en Railway
- [ ] Código actualizado y desplegado

---

## 🎯 Comandos Rápidos (Copy & Paste)

### Verificar recurrent (PowerShell)

```powershell
$env:PGPASSWORD='TU_PASSWORD'
psql -h TU_HOST -p 5432 -U postgres -d railway -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'class_schedule_patterns' AND column_name = 'recurrent';"
```

### Agregar recurrent directamente (PowerShell)

```powershell
$env:PGPASSWORD='TU_PASSWORD'
psql -h TU_HOST -p 5432 -U postgres -d railway -c "ALTER TABLE class_schedule_patterns ADD COLUMN IF NOT EXISTS recurrent BOOLEAN NOT NULL DEFAULT FALSE;"
```

---

**¿Necesitas ayuda?** Ejecuta el script `verify-and-fix-migrations-railway.ps1` para una verificación automática.

