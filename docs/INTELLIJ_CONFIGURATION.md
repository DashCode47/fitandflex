# 🔧 Configuración de IntelliJ IDEA para Fit & Flex

## 📋 Cómo Verificar y Configurar el Perfil Activo

### Método 1: Configurar en Run Configuration

1. **Click derecho** en `FitandflexApplication.java`
2. Selecciona **"Modify Run Configuration..."** o **"Edit Configurations..."**
3. O ve a: **Run** → **Edit Configurations...**

4. En la sección **"Environment variables"** o **"Program arguments"**, agrega:

   **Para usar perfil DEV (base de datos local `fitandflex_dev`):**
   ```
   --spring.profiles.active=dev
   ```
   
   **Para usar perfil PROD (base de datos local `fitandflex_prod`):**
   ```
   --spring.profiles.active=prod
   ```
   
   **Para NO usar ningún perfil (usa `application.properties` por defecto):**
   ```
   (dejar vacío)
   ```

5. Click en **"Apply"** y **"OK"**

### Método 2: Usar Variables de Entorno

En la misma ventana de configuración, en **"Environment variables"**, puedes agregar:

```
SPRING_PROFILES_ACTIVE=dev
```

### Método 3: Verificar en los Logs

Cuando ejecutas la aplicación, busca en la consola estas líneas:

```
The following 1 profile is active: "dev"
```

O si no hay perfil activo:
```
No active profile set, falling back to default properties
```

También puedes buscar la línea de conexión a la base de datos:
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection jdbc:postgresql://localhost:5432/fitandflex_dev
```

---

## 🔍 Qué Configuración se Usa Según el Perfil

### Sin Perfil (por defecto)
- **Archivo:** `application.properties`
- **Base de datos:** `localhost:5432/fitandflex_prod`
- **Usuario:** `fitandflex_prod`
- **Password:** `fitandflex_prod`

### Perfil DEV (`--spring.profiles.active=dev`)
- **Archivo:** `application-dev.properties`
- **Base de datos:** `localhost:5432/fitandflex_dev`
- **Usuario:** `fitandflex_dev`
- **Password:** `fitandflex_dev123`
- **DDL:** `create-drop` (recrea tablas en cada inicio)

### Perfil PROD (`--spring.profiles.active=prod`)
- **Archivo:** `application-prod.properties`
- **Base de datos:** `localhost:5432/fitandflex_prod` (por defecto)
- **Usuario:** `fitandflex_prod` (por defecto)
- **Password:** Vacío (requiere variable de entorno `DATABASE_PASSWORD`)
- **DDL:** `update` (actualiza esquema)

---

## 📝 Pasos Rápidos para Configurar

1. **Abrir configuración de ejecución:**
   - Click derecho en `FitandflexApplication.java`
   - **"Modify Run Configuration..."**

2. **Agregar argumento:**
   - En **"Program arguments"** o **"VM options"**, agrega:
   ```
   --spring.profiles.active=dev
   ```

3. **Guardar y ejecutar:**
   - Click **"Apply"** → **"OK"**
   - Ejecuta con el icono de play ▶️

4. **Verificar en logs:**
   - Busca: `The following 1 profile is active: "dev"`
   - Busca la URL de conexión a la base de datos

---

## 🎯 Recomendación para Desarrollo Local

**Usa el perfil DEV** para desarrollo local:

```
--spring.profiles.active=dev
```

Esto te dará:
- ✅ Base de datos local: `fitandflex_dev`
- ✅ Logs detallados (DEBUG)
- ✅ SQL visible en consola
- ✅ Hot reload habilitado

