\# Fit & Flex - Backend

Backend de gestión para la cadena de estudios de yoga \*\*Fit & Flex\*\*, desarrollado en \*\*Java Spring Boot\*\*.

Soporta múltiples sucursales, roles de usuario y manejo de clases, reservas, pagos y productos.

\---

## 📚 Documentación

Toda la documentación del proyecto está organizada en la carpeta [`docs/`](./docs/).

**📖 [Ver Índice de Documentación](./docs/INDEX.md)**

### Documentación Principal:
- **[Resumen de Controladores](./docs/RESUMEN_CONTROLADORES.md)** - Todos los endpoints de la API
- **[JWT Refresh Token Guide](./docs/JWT_REFRESH_TOKEN_GUIDE.md)** - Autenticación y tokens
- **[Database Setup](./docs/DATABASE_SETUP.md)** - Configuración de base de datos
- **[Deployment](./docs/DEPLOYMENT.md)** - Guía de despliegue
- **[Project Overview](./docs/PROJECT_OVERVIEW_AND_ROADMAP.md)** - Visión general del proyecto

\---

\## 🏛 Arquitectura

El proyecto está organizado siguiendo una \*\*arquitectura limpia (Clean Architecture / Hexagonal)\*\*:

src/main/java/com/fitflex/├── config/ -> Configuración de seguridad, JWT, CORS, etc.├── controller/ -> Controladores REST (endpoints)├── dto/ -> Data Transfer Objects (requests/responses)├── entity/ -> Entidades JPA (Role, User, Branch, Clase, Reserva, Pago, Producto)├── repository/ -> Interfaces JPA para acceso a la base de datos├── service/ -> Lógica de negocio y servicios├── security/ -> Seguridad (roles, JWT, autenticación)└── exception/ -> Manejo global de errores y excepciones

\---

\## ⚙ Dependencias principales

\- \*\*Spring Web\*\* → REST APIs

\- \*\*Spring Data JPA\*\* → ORM para PostgreSQL

\- \*\*PostgreSQL Driver\*\* → Conexión a la base de datos

\- \*\*Spring Security\*\* → Autenticación y autorización (JWT)

\- \*\*Spring Validation\*\* → Validación de DTOs

\- \*\*Lombok\*\* → Reducción de boilerplate (getters/setters, builders)

\- \*\*Spring Boot Actuator\*\* → Monitoreo del sistema

\---

\## 🗂 Estructura de la Base de Datos

\### Entidades principales:

\- \*\*Role\*\* → Roles del sistema (\`SUPER\_ADMIN\`, \`BRANCH\_ADMIN\`, \`USER\`)

\- \*\*Branch\*\* → Sucursales de la cadena

\- \*\*User\*\* → Usuarios del sistema (con rol y sucursal asignada)

\- \*\*Clase\*\* → Clases de yoga ofrecidas

\- \*\*Reservation\*\* → Reservaciones de usuarios

\- \*\*Payment\*\* → Pagos realizados por usuarios

\- \*\*Product\*\* → Productos o servicios adicionales

\### Relaciones

\- \*\*User\*\* → Many-to-One → Role

\- \*\*User\*\* → Many-to-One → Branch

\- \*\*Branch\*\* → One-to-Many → Users

\- \*\*Role\*\* → One-to-Many → Users

\- \*\*Clase\*\* → Many-to-One → Branch

\- \*\*Reservation\*\* → Many-to-One → User y Clase

\- \*\*Payment\*\* → Many-to-One → User y Reservation

\- \*\*Product\*\* → Many-to-One → Branch

\> Todas las entidades incluyen timestamps (\`createdAt\`, \`updatedAt\`) para auditoría.

\---

\## 🔑 Gestión de Roles y Seguridad

\- Roles principales: \`SUPER\_ADMIN\`, \`BRANCH\_ADMIN\`, \`USER\`.

\- JWT para autenticación y autorización.

\- Acceso a recursos limitado según rol:

\- \*\*SUPER\_ADMIN\*\* → acceso completo a todas las sucursales y reportes globales.

\- \*\*BRANCH\_ADMIN\*\* → acceso solo a su sucursal.

\- \*\*USER\*\* → acceso a sus reservas, pagos y horarios disponibles.

\---

\## 🚀 Cómo correr el proyecto

1\. Clonar el repositorio

\`\`\`bash

git clone

cd fitandflex-backend