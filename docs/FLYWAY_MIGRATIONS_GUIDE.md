# 📦 Guía de Migraciones de Base de Datos con Flyway

## Índice
- [¿Qué es Flyway?](#qué-es-flyway)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Convención de Nombres](#convención-de-nombres)
- [Cómo Crear una Nueva Migración](#cómo-crear-una-nueva-migración)
- [Ejemplos de Migraciones Comunes](#ejemplos-de-migraciones-comunes)
- [Flujo de Trabajo](#flujo-de-trabajo)
- [Comandos Útiles](#comandos-útiles)
- [Mejores Prácticas](#mejores-prácticas)
- [Solución de Problemas](#solución-de-problemas)

---

## ¿Qué es Flyway?

Flyway es una herramienta de control de versiones para bases de datos. Funciona como Git, pero para tu esquema de base de datos:

- ✅ Mantiene un historial de todos los cambios
- ✅ Aplica cambios automáticamente al iniciar la aplicación
- ✅ Asegura que todas las instancias (local, staging, prod) tengan el mismo esquema
- ✅ Previene modificaciones accidentales del esquema

### ¿Cómo Funciona?

```
┌─────────────────────────────────────────────────────────────┐
│  1. La aplicación inicia                                    │
│                    ↓                                        │
│  2. Flyway revisa la tabla 'flyway_schema_history'          │
│                    ↓                                        │
│  3. Compara con archivos en db/migration/                   │
│                    ↓                                        │
│  4. Ejecuta migraciones pendientes en orden                 │
│                    ↓                                        │
│  5. Registra cada migración exitosa en el historial         │
└─────────────────────────────────────────────────────────────┘
```

---

## Estructura del Proyecto

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__baseline_schema.sql        # Esquema inicial
        ├── V2__add_user_avatar.sql        # Migración 2
        ├── V3__create_notifications.sql   # Migración 3
        └── V4__add_payment_metadata.sql   # Migración 4
```

---

## Convención de Nombres

### Formato Obligatorio

```
V{VERSION}__{DESCRIPCION}.sql
```

| Parte | Descripción | Ejemplo |
|-------|-------------|---------|
| `V` | Prefijo obligatorio (Version) | `V` |
| `{VERSION}` | Número de versión | `1`, `2`, `10`, `1.1` |
| `__` | Doble guión bajo (separador) | `__` |
| `{DESCRIPCION}` | Descripción con guiones bajos | `create_users_table` |
| `.sql` | Extensión del archivo | `.sql` |

### Ejemplos Válidos ✅

```
V1__baseline_schema.sql
V2__add_user_phone_field.sql
V3__create_notifications_table.sql
V4__add_index_to_reservations.sql
V5__alter_products_add_discount.sql
V10__refactor_payment_status.sql
```

### Ejemplos Inválidos ❌

```
v1__lowercase.sql           # ❌ 'v' debe ser mayúscula
V1_single_underscore.sql    # ❌ Necesita doble guión bajo
2__missing_v_prefix.sql     # ❌ Falta el prefijo 'V'
V1__with spaces.sql         # ❌ No usar espacios
V1__.sql                    # ❌ Descripción vacía
```

---

## Cómo Crear una Nueva Migración

### Paso 1: Identificar el Siguiente Número de Versión

Revisa el último archivo en `db/migration/` y usa el siguiente número:

```bash
# Si el último es V3__xxx.sql, tu nuevo archivo será V4__xxx.sql
```

### Paso 2: Crear el Archivo SQL

Crea un nuevo archivo en `src/main/resources/db/migration/`:

```sql
-- V4__add_user_preferences.sql

-- Agregar columna de preferencias a usuarios
ALTER TABLE users ADD COLUMN preferences JSONB;

-- Agregar columna de idioma preferido
ALTER TABLE users ADD COLUMN preferred_language VARCHAR(5) DEFAULT 'es';

-- Crear índice para búsquedas en preferencias
CREATE INDEX idx_user_preferences ON users USING GIN (preferences);
```

### Paso 3: Actualizar la Entidad Java (si aplica)

```java
// User.java
@Column(columnDefinition = "jsonb")
private String preferences;

@Column(name = "preferred_language", length = 5)
private String preferredLanguage = "es";
```

### Paso 4: Probar Localmente

```bash
# Inicia la aplicación - Flyway aplicará la migración automáticamente
./gradlew bootRun
```

### Paso 5: Verificar

Revisa los logs de la aplicación:

```
Flyway Community Edition 9.x.x
Database: jdbc:postgresql://localhost:5432/fitandflex_db (PostgreSQL 15.x)
Successfully validated 4 migrations
Current version of schema "public": 3
Migrating schema "public" to version "4 - add user preferences"
Successfully applied 1 migration
```

---

## Ejemplos de Migraciones Comunes

### 1. Agregar una Nueva Columna

```sql
-- V5__add_user_last_login.sql
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE;
```

### 2. Agregar Columna con Valor por Defecto

```sql
-- V6__add_product_featured_flag.sql
ALTER TABLE products ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE;
```

### 3. Crear una Nueva Tabla

```sql
-- V7__create_notifications_table.sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_read ON notifications(read);
CREATE INDEX idx_notification_created ON notifications(created_at DESC);
```

### 4. Agregar un Índice

```sql
-- V8__add_index_payments_date.sql
CREATE INDEX CONCURRENTLY idx_payment_created_at ON payments(created_at);
```

> ⚠️ **Nota**: `CONCURRENTLY` permite crear el índice sin bloquear la tabla (importante en producción).

### 5. Modificar Tipo de Columna

```sql
-- V9__expand_description_length.sql
ALTER TABLE products ALTER COLUMN description TYPE VARCHAR(2000);
```

### 6. Renombrar una Columna

```sql
-- V10__rename_user_name_to_full_name.sql
ALTER TABLE users RENAME COLUMN name TO full_name;
```

### 7. Agregar Foreign Key

```sql
-- V11__add_instructor_to_classes.sql
ALTER TABLE classes ADD COLUMN instructor_id BIGINT;
ALTER TABLE classes ADD CONSTRAINT fk_class_instructor 
    FOREIGN KEY (instructor_id) REFERENCES users(id);
CREATE INDEX idx_class_instructor ON classes(instructor_id);
```

### 8. Eliminar una Columna (con cuidado)

```sql
-- V12__remove_deprecated_field.sql
-- IMPORTANTE: Asegúrate de que el código ya no use este campo
ALTER TABLE users DROP COLUMN IF EXISTS legacy_field;
```

### 9. Agregar Constraint Único

```sql
-- V13__add_unique_product_sku.sql
ALTER TABLE products ADD CONSTRAINT uk_product_sku_branch 
    UNIQUE (sku, branch_id);
```

### 10. Insertar Datos de Configuración

```sql
-- V14__add_new_role.sql
INSERT INTO roles (name, description, created_at) 
VALUES ('RECEPTIONIST', 'Recepcionista de sucursal', NOW())
ON CONFLICT (name) DO NOTHING;
```

---

## Flujo de Trabajo

### Desarrollo Local

```
1. Identificas que necesitas un cambio en la BD
         ↓
2. Creas el archivo V{N}__descripcion.sql
         ↓
3. Actualizas la entidad Java correspondiente
         ↓
4. Ejecutas la aplicación localmente
         ↓
5. Flyway aplica la migración automáticamente
         ↓
6. Verificas que funciona correctamente
         ↓
7. Commit y Push
```

### Despliegue a Railway

```
1. Push a tu rama main/master
         ↓
2. Railway detecta el cambio y redeploya
         ↓
3. La aplicación inicia en Railway
         ↓
4. Flyway detecta migraciones pendientes
         ↓
5. Aplica las migraciones automáticamente
         ↓
6. La aplicación está lista con el nuevo esquema
```

---

## Comandos Útiles

### Desde la Aplicación (automático)

La aplicación ejecuta migraciones automáticamente al iniciar. No necesitas hacer nada extra.

### Desde Gradle (opcional)

```bash
# Ver estado de todas las migraciones
./gradlew flywayInfo

# Ejemplo de salida:
# +-----------+---------+---------------------+------+---------------------+
# | Version   | State   | Description         | Type | Installed On        |
# +-----------+---------+---------------------+------+---------------------+
# | 1         | Success | baseline schema     | SQL  | 2024-01-15 10:30:00 |
# | 2         | Success | add user avatar     | SQL  | 2024-01-20 14:15:00 |
# | 3         | Pending | create notifications| SQL  |                     |
# +-----------+---------+---------------------+------+---------------------+

# Ejecutar migraciones manualmente
./gradlew flywayMigrate

# Validar que las migraciones son correctas
./gradlew flywayValidate

# Reparar el historial si algo falló
./gradlew flywayRepair
```

### Consultar Historial en la Base de Datos

```sql
-- Ver todas las migraciones aplicadas
SELECT version, description, installed_on, execution_time, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

---

## Mejores Prácticas

### ✅ Hacer

1. **Una migración = un cambio lógico**
   ```
   V5__add_user_avatar.sql         # Bien: un cambio específico
   V6__add_notifications_system.sql # Bien: sistema completo pero relacionado
   ```

2. **Nombres descriptivos**
   ```
   V5__add_user_avatar_url.sql     # ✅ Claro
   V5__update.sql                   # ❌ Muy vago
   ```

3. **Siempre probar localmente primero**

4. **Usar IF EXISTS / IF NOT EXISTS para idempotencia**
   ```sql
   CREATE TABLE IF NOT EXISTS ...
   DROP INDEX IF EXISTS ...
   ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...
   ```

5. **Comentar migraciones complejas**
   ```sql
   -- Esta migración separa el campo 'address' en componentes individuales
   -- Pasos: 1) Crear nuevas columnas, 2) Migrar datos, 3) Eliminar columna vieja
   ```

6. **Hacer backup antes de migraciones destructivas en producción**

### ❌ No Hacer

1. **NUNCA modificar una migración ya aplicada**
   - Si ya hiciste commit/deploy, crea una nueva migración para corregir

2. **NUNCA eliminar archivos de migración**
   - Flyway espera que existan para validar el historial

3. **NUNCA cambiar el orden de las versiones**
   - V1, V2, V3... siempre en orden ascendente

4. **EVITAR migraciones muy grandes**
   - Dividir en pasos más pequeños si es posible

5. **NO usar DDL que bloquee tablas por mucho tiempo en producción**
   ```sql
   -- ❌ Puede bloquear la tabla por mucho tiempo
   CREATE INDEX idx_big_table ON big_table(column);
   
   -- ✅ Mejor: crear sin bloqueo
   CREATE INDEX CONCURRENTLY idx_big_table ON big_table(column);
   ```

---

## Solución de Problemas

### Error: "Migration checksum mismatch"

**Causa**: Modificaste un archivo de migración que ya fue aplicado.

**Solución**:
```bash
# Opción 1: Reparar (si el cambio fue intencional)
./gradlew flywayRepair

# Opción 2: Revertir el cambio al archivo original
git checkout -- src/main/resources/db/migration/V{N}__xxx.sql
```

### Error: "Found non-empty schema without schema history table"

**Causa**: La base de datos tiene tablas pero no tiene historial de Flyway.

**Solución**: Ya está configurado `baseline-on-migrate=true`, debería resolverse automáticamente.

### Error: "Migration V{N} failed"

**Causa**: Error en el SQL de la migración.

**Solución**:
1. Revisa los logs para ver el error SQL específico
2. Corrige el problema en la base de datos manualmente
3. Ejecuta `./gradlew flywayRepair`
4. Corrige el archivo de migración (si aún no fue a producción)
5. Reinicia la aplicación

### La migración no se ejecuta

**Posibles causas**:
1. El nombre del archivo no sigue la convención
2. La versión ya existe en el historial
3. El archivo está en la carpeta incorrecta

**Verificar**:
```bash
./gradlew flywayInfo
```

### Quiero empezar de cero localmente

```bash
# Solo en desarrollo local, NUNCA en producción
./gradlew flywayClean flywayMigrate
```

O simplemente elimina la base de datos local y créala de nuevo.

---

## Configuración por Ambiente

| Propiedad | Local | Dev | Prod |
|-----------|-------|-----|------|
| `flyway.enabled` | true | true | true |
| `flyway.clean-disabled` | false | false | **true** |
| `flyway.baseline-on-migrate` | true | true | true |

> ⚠️ **IMPORTANTE**: `clean-disabled=true` en producción previene borrado accidental de datos.

---

## Recursos Adicionales

- [Documentación oficial de Flyway](https://flywaydb.org/documentation/)
- [Flyway con Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [PostgreSQL DDL Reference](https://www.postgresql.org/docs/current/ddl.html)

---

*Última actualización: Diciembre 2024*

