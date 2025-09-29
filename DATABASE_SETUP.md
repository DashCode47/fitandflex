# 🗄️ Configuración de Base de Datos - Fit & Flex

## 📋 Requisitos Previos

- **PostgreSQL 12+** instalado y ejecutándose
- **Java 17+** instalado
- **Gradle** (incluido en el proyecto)

## 🚀 Configuración Paso a Paso

### 1. Instalar PostgreSQL

#### Windows:
```bash
# Descargar desde: https://www.postgresql.org/download/windows/
# O usar Chocolatey:
choco install postgresql
```

#### macOS:
```bash
# Usar Homebrew:
brew install postgresql
brew services start postgresql
```

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. Crear Base de Datos y Usuario

```sql
-- Conectar como superusuario
sudo -u postgres psql

-- Crear base de datos
CREATE DATABASE fitandflex_db;

-- Crear usuario
CREATE USER fitandflex_user WITH PASSWORD 'fitandflex_password';

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE fitandflex_db TO fitandflex_user;

-- Conectar a la base de datos
\c fitandflex_db;

-- Otorgar permisos en el esquema
GRANT ALL ON SCHEMA public TO fitandflex_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fitandflex_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fitandflex_user;

-- Salir
\q
```

### 3. Ejecutar Script de Inicialización

```bash
# Ejecutar el script SQL de inicialización
psql -U fitandflex_user -d fitandflex_db -f src/main/resources/sql/init-database.sql
```

### 4. Verificar Configuración

```bash
# Verificar que la base de datos se creó correctamente
psql -U fitandflex_user -d fitandflex_db -c "\dt"
```

## 🔧 Configuración por Perfiles

### Desarrollo (application-dev.properties)
```properties
spring.profiles.active=dev
```
- Base de datos: `fitandflex_dev`
- DDL: `create-drop` (recrea tablas en cada inicio)
- Logging: DEBUG habilitado

### Producción (application-prod.properties)
```properties
spring.profiles.active=prod
```
- Base de datos: `fitandflex_prod`
- DDL: `validate` (solo valida esquema)
- Logging: INFO/WARN
- Variables de entorno para credenciales

### Testing (application-test.properties)
```properties
spring.profiles.active=test
```
- Base de datos: H2 en memoria
- DDL: `create-drop`
- Logging: WARN

## 🚀 Ejecutar la Aplicación

### Desarrollo:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Producción:
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Testing:
```bash
./gradlew test
```

## 📊 Estructura de la Base de Datos

### Tablas Principales:
- **roles** - Roles del sistema
- **branches** - Sucursales
- **users** - Usuarios
- **clases** - Clases de yoga
- **reservations** - Reservaciones
- **payments** - Pagos
- **products** - Productos

### Relaciones:
- Users → Roles (Many-to-One)
- Users → Branches (Many-to-One)
- Clases → Branches (Many-to-One)
- Clases → Users/Instructors (Many-to-One)
- Reservations → Users (Many-to-One)
- Reservations → Clases (Many-to-One)
- Payments → Users (Many-to-One)
- Payments → Reservations (Many-to-One)
- Products → Branches (Many-to-One)

## 🔍 Verificación de Conexión

### 1. Verificar que la aplicación se conecta:
```bash
# Ejecutar la aplicación y verificar logs
./gradlew bootRun
```

### 2. Verificar endpoints de salud:
```bash
curl http://localhost:8080/actuator/health
```

### 3. Verificar base de datos:
```sql
-- Conectar a la base de datos
psql -U fitandflex_user -d fitandflex_db

-- Verificar tablas
\dt

-- Verificar datos iniciales
SELECT * FROM roles;
SELECT * FROM branches;
```

## 🛠️ Solución de Problemas

### Error de Conexión:
1. Verificar que PostgreSQL esté ejecutándose
2. Verificar credenciales en `application.properties`
3. Verificar que el puerto 5432 esté disponible

### Error de Permisos:
1. Verificar que el usuario tenga permisos en la base de datos
2. Ejecutar comandos GRANT necesarios

### Error de Esquema:
1. Verificar que el script de inicialización se ejecutó correctamente
2. Revisar logs de Hibernate para errores de DDL

## 📝 Notas Importantes

- **Desarrollo**: Usa `create-drop` para recrear tablas automáticamente
- **Producción**: Usa `validate` para solo validar el esquema
- **Testing**: Usa H2 en memoria para tests rápidos
- **Logs**: En desarrollo se muestran todas las consultas SQL
- **Pool de Conexiones**: Configurado para máximo 20 conexiones simultáneas
