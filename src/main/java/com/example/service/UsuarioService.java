package com.example.service;

import com.example.model.Usuario;
import com.example.repository.UsuarioRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

@ApplicationScoped
public class UsuarioService {

    private static final Logger LOG = Logger.getLogger(UsuarioService.class);

    @Inject
    UsuarioRepository usuarioRepository;

    public Multi<Usuario> obtenerTodos() {
        LOG.debug("Obteniendo todos los usuarios activos");
        return usuarioRepository.findActivos()
                .onFailure().invoke(throwable -> 
                    LOG.error("Error al obtener usuarios", throwable));
    }

    public Uni<Usuario> obtenerPorId(String id) {
        LOG.debugf("Obteniendo usuario por ID: %s", id);
        
        if (!ObjectId.isValid(id)) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("ID de usuario inválido"));
        }

        return usuarioRepository.findByIdAndActivo(new ObjectId(id))
                .onItem().ifNull().failWith(() -> 
                    new RuntimeException("Usuario no encontrado"))
                .onFailure().invoke(throwable -> 
                    LOG.errorf(throwable, "Error al obtener usuario con ID: %s", id));
    }

    public Uni<Usuario> crear(@Valid Usuario usuario) {
        LOG.debugf("Creando nuevo usuario: %s", usuario.email);
        
        return usuarioRepository.existsByEmail(usuario.email)
                .flatMap(exists -> {
                    if (exists) {
                        return Uni.createFrom().failure(
                            new IllegalArgumentException("Ya existe un usuario con este email"));
                    }
                    return usuarioRepository.persist(usuario);
                })
                .onItem().invoke(usuarioCreado -> 
                    LOG.infof("Usuario creado exitosamente: %s", usuarioCreado.id))
                .onFailure().invoke(throwable -> 
                    LOG.errorf(throwable, "Error al crear usuario: %s", usuario.email));
    }

    public Uni<Usuario> actualizar(String id, @Valid Usuario usuarioActualizado) {
        LOG.debugf("Actualizando usuario con ID: %s", id);
        
        if (!ObjectId.isValid(id)) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("ID de usuario inválido"));
        }

        ObjectId objectId = new ObjectId(id);

        return usuarioRepository.existsByEmailAndNotId(usuarioActualizado.email, objectId)
                .flatMap(exists -> {
                    if (exists) {
                        return Uni.createFrom().failure(
                            new IllegalArgumentException("Ya existe otro usuario con este email"));
                    }
                    
                    return usuarioRepository.findByIdAndActivo(objectId)
                            .onItem().ifNull().failWith(() -> 
                                new RuntimeException("Usuario no encontrado"))
                            .flatMap(usuarioExistente -> {
                                usuarioExistente.nombre = usuarioActualizado.nombre;
                                usuarioExistente.email = usuarioActualizado.email;
                                usuarioExistente.telefono = usuarioActualizado.telefono;
                                usuarioExistente.actualizar();
                                
                                return usuarioRepository.update(usuarioExistente);
                            });
                })
                .onItem().invoke(usuarioAct -> 
                    LOG.infof("Usuario actualizado exitosamente: %s", usuarioAct.id))
                .onFailure().invoke(throwable -> 
                    LOG.errorf(throwable, "Error al actualizar usuario con ID: %s", id));
    }

    public Uni<Boolean> eliminar(String id) {
        LOG.debugf("Eliminando usuario con ID: %s", id);
        
        if (!ObjectId.isValid(id)) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("ID de usuario inválido"));
        }

        return usuarioRepository.findByIdAndActivo(new ObjectId(id))
                .onItem().ifNull().failWith(() -> 
                    new RuntimeException("Usuario no encontrado"))
                .flatMap(usuario -> {
                    usuario.activo = false;
                    usuario.actualizar();
                    return usuarioRepository.update(usuario);
                })
                .map(usuario -> true)
                .onItem().invoke(eliminado -> 
                    LOG.infof("Usuario eliminado exitosamente: %s", id))
                .onFailure().invoke(throwable -> 
                    LOG.errorf(throwable, "Error al eliminar usuario con ID: %s", id));
    }

    public Multi<Usuario> buscarPorNombre(String nombre) {
        LOG.debugf("Buscando usuarios por nombre: %s", nombre);
        return usuarioRepository.findByNombre(nombre)
                .onFailure().invoke(throwable -> 
                    LOG.errorf(throwable, "Error al buscar usuarios por nombre: %s", nombre));
    }

    public Uni<Long> contarUsuarios() {
        LOG.debug("Contando usuarios activos");
        return usuarioRepository.countActivos()
                .onFailure().invoke(throwable -> 
                    LOG.error("Error al contar usuarios", throwable));
    }
}