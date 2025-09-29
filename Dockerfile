# ===========================================
# FIT & FLEX - DOCKERFILE
# ===========================================

# Usar imagen base de OpenJDK 17
FROM openjdk:17-jdk-slim

# Información del mantenedor
LABEL maintainer="Fit & Flex Team <support@fitandflex.com>"
LABEL description="Fit & Flex Backend API"

# Crear usuario no-root para seguridad
RUN groupadd -r fitandflex && useradd -r -g fitandflex fitandflex

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR de la aplicación
COPY build/libs/fitandflex-*.jar app.jar

# Cambiar propietario del archivo JAR
RUN chown fitandflex:fitandflex app.jar

# Cambiar al usuario no-root
USER fitandflex

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
