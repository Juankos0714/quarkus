# API REST Reactivo con Quarkus y MongoDB

Este proyecto implementa un API REST completamente reactivo usando Quarkus y MongoDB con pool de conexiones optimizado.

## Características

- **Programación Reactiva**: Usa Mutiny Uni y Multi para operaciones no bloqueantes
- **Pool de Conexiones**: Configuración optimizada para alta concurrencia
- **Validación**: Bean Validation para entrada de datos
- **Documentación**: OpenAPI/Swagger automático
- **Health Checks**: Monitoreo de salud de la base de datos
- **Logging**: Logging estructurado y configurable

## Estructura del Proyecto

```
src/
├── main/java/com/example/
│   ├── controller/        # Controladores REST
│   ├── service/          # Lógica de negocio
│   ├── repository/       # Acceso a datos
│   ├── model/           # Entidades
│   └── health/          # Health checks
└── test/java/           # Pruebas
```

## Tecnologías

- **Quarkus 3.6.4**: Framework Java nativo para la nube
- **MongoDB**: Base de datos NoSQL
- **Mutiny**: Biblioteca de programación reactiva
- **RESTEasy Reactive**: Cliente REST reactivo
- **Bean Validation**: Validación de datos
- **OpenAPI**: Documentación de API

## Configuración MongoDB

```properties
# Pool de conexiones optimizado
quarkus.mongodb.max-pool-size=20
quarkus.mongodb.min-pool-size=5
quarkus.mongodb.max-connection-idle-time=30s
quarkus.mongodb.max-connection-life-time=1h
quarkus.mongodb.connection-timeout=10s
quarkus.mongodb.read-timeout=30s
quarkus.mongodb.server-selection-timeout=5s
```

## Endpoints

### Usuarios
- `GET /api/usuarios` - Listar todos los usuarios
- `GET /api/usuarios/{id}` - Obtener usuario por ID
- `POST /api/usuarios` - Crear usuario
- `PUT /api/usuarios/{id}` - Actualizar usuario
- `DELETE /api/usuarios/{id}` - Eliminar usuario (soft delete)
- `GET /api/usuarios/buscar?nombre={nombre}` - Buscar por nombre
- `GET /api/usuarios/count` - Contar usuarios

### Documentación y Monitoreo
- `/swagger-ui` - Interfaz Swagger
- `/api-docs` - Documentación OpenAPI
- `/health` - Health checks

## Ejecutar la Aplicación

### Desarrollo
```bash
./mvnw quarkus:dev
```

### Producción
```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### Nativo
```bash
./mvnw clean package -Pnative
./target/quarkus-mongodb-reactive-1.0.0-SNAPSHOT-runner
```

## Pruebas

```bash
./mvnw test
```

## Ejemplo de Uso

### Crear Usuario
```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "telefono": "1234567890"
  }'
```

### Obtener Usuarios
```bash
curl http://localhost:8080/api/usuarios
```

## Características Reactivas

- **Sin Bloqueo**: Todas las operaciones usan Uni/Multi
- **Pool de Conexiones**: Gestión eficiente de conexiones MongoDB
- **Backpressure**: Manejo automático de presión hacia atrás
- **Error Handling**: Manejo robusto de errores reactivos
- **Logging**: Trazabilidad completa de operaciones

La aplicación está optimizada para alta concurrencia y rendimiento, aprovechando las capacidades reactivas de Quarkus y MongoDB.