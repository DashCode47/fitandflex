# 🔧 Solución Rápida: Agregar Columna `recurrent`

## ✅ Diagnóstico
La consulta verificó que la columna `recurrent` NO existe en `class_schedule_patterns`.

## 🚀 Solución: Ejecutar este SQL

Copia y pega este SQL completo en DBeaver o psql:

```sql
-- ===========================================
-- AGREGAR COLUMNA RECURRENT A class_schedule_patterns
-- ===========================================

-- Agregar el campo recurrent si no existe
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
        
        -- Crear índice para el campo recurrent
        CREATE INDEX IF NOT EXISTS idx_schedule_pattern_recurrent 
        ON class_schedule_patterns(recurrent);
        
        RAISE NOTICE 'Campo recurrent agregado exitosamente a class_schedule_patterns';
    ELSE
        RAISE NOTICE 'El campo recurrent ya existe en class_schedule_patterns';
    END IF;
END $$;
```

## ✅ Verificar después de ejecutar

Ejecuta nuevamente esta consulta para confirmar:

```sql
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'class_schedule_patterns'
AND column_name = 'recurrent';
```

**Deberías ver:**
```
column_name | data_type | column_default | is_nullable
------------|-----------|----------------|-------------
recurrent   | boolean   | false          | NO
```

## 🔄 Después de ejecutar

1. ✅ Ejecuta el SQL de arriba
2. ✅ Verifica con la consulta de verificación
3. ✅ Reinicia la aplicación en Railway Dashboard
4. ✅ Prueba el endpoint `/api/classes/active` nuevamente

## 🐛 Si sigue sin funcionar

Si después de ejecutar el SQL y reiniciar la aplicación sigue el error:

1. **Verifica que ejecutaste el SQL en la base de datos correcta de Railway**
2. **Verifica que reiniciaste la aplicación en Railway**
3. **Revisa los logs de Railway para ver si hay otros errores**

