# Multi-stage build para optimizar el tamaño de la imagen

# Etapa 1: Build de la aplicación
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar archivos de configuración Maven
COPY pom.xml .

# Descargar dependencias (se cachea si no cambia pom.xml)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución
FROM registry.access.redhat.com/ubi8/openjdk-17:1.18

ENV LANGUAGE='en_US:en'

# Instalar curl para health checks y configurar permisos
USER root
RUN microdnf install curl ca-certificates \
    && microdnf clean all \
    && chown 1001:root /deployments \
    && chmod "g+rwX" /deployments

# Cambiar a usuario no-root
USER 1001

# Copiar la aplicación compilada
COPY --from=build --chown=1001 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=1001 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=1001 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=1001 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

# Exponer puerto
EXPOSE 8080

# Variables de entorno
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health/ready || exit 1

# Comando de inicio
ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]