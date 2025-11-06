# ===========================================
# FIT & FLEX - DOCKERFILE
# ===========================================

# Usar imagen base de OpenJDK 17 con Gradle para construir la aplicación
FROM eclipse-temurin:17-jdk-alpine

# Información del mantenedor
LABEL maintainer="Fit & Flex Team <support@fitandflex.com>"
LABEL description="Fit & Flex Backend API"

# Instalar herramientas necesarias
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Crear usuario no-root para seguridad
RUN groupadd -r fitandflex && useradd -r -g fitandflex fitandflex

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Hacer el gradlew ejecutable
RUN chmod +x gradlew

# Copiar código fuente
COPY src src

# Construir la aplicación
RUN ./gradlew build -x test

# Copiar el JAR construido (encontrar y copiar el JAR principal)
RUN find build/libs -name "*.jar" -not -name "*-plain.jar" | head -1 | xargs -I {} cp {} app.jar

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
