// Script de inicialización para MongoDB
// Este archivo se ejecuta automáticamente cuando se crea el contenedor

// Cambiar a la base de datos de la aplicación
db = db.getSiblingDB('reactive_api_db');

// Crear un usuario específico para la aplicación (opcional)
db.createUser({
    user: 'app_user',
    pwd: 'app_password',
    roles: [
        {
            role: 'readWrite',
            db: 'reactive_api_db'
        }
    ]
});

// Crear la colección de usuarios con validación
db.createCollection('usuarios', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['nombre', 'email', 'activo'],
            properties: {
                nombre: {
                    bsonType: 'string',
                    description: 'Nombre del usuario - requerido'
                },
                email: {
                    bsonType: 'string',
                    pattern: '^.+@.+$',
                    description: 'Email válido - requerido'
                },
                telefono: {
                    bsonType: 'string',
                    description: 'Teléfono del usuario'
                },
                activo: {
                    bsonType: 'bool',
                    description: 'Estado activo del usuario - requerido'
                },
                fechaCreacion: {
                    bsonType: 'date',
                    description: 'Fecha de creación'
                },
                fechaActualizacion: {
                    bsonType: 'date',
                    description: 'Fecha de última actualización'
                }
            }
        }
    }
});

// Crear índices para optimizar las consultas
db.usuarios.createIndex({ email: 1 }, { unique: true });
db.usuarios.createIndex({ nombre: 1 });
db.usuarios.createIndex({ activo: 1 });
db.usuarios.createIndex({ fechaCreacion: 1 });

// Insertar algunos datos de ejemplo
db.usuarios.insertMany([
    {
        nombre: 'Juan Pérez',
        email: 'juan@example.com',
        telefono: '1234567890',
        activo: true,
        fechaCreacion: new Date(),
        fechaActualizacion: new Date()
    },
    {
        nombre: 'María García',
        email: 'maria@example.com',
        telefono: '0987654321',
        activo: true,
        fechaCreacion: new Date(),
        fechaActualizacion: new Date()
    },
    {
        nombre: 'Carlos López',
        email: 'carlos@example.com',
        telefono: '5555555555',
        activo: true,
        fechaCreacion: new Date(),
        fechaActualizacion: new Date()
    }
]);

print('Base de datos inicializada correctamente con datos de ejemplo');
print('Colección: usuarios');
print('Índices creados: email (único), nombre, activo, fechaCreacion');
print('Documentos insertados: 3 usuarios de ejemplo');