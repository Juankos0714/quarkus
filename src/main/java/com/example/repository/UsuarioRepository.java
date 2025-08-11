package com.example.repository;

import com.example.model.Usuario;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements ReactivePanacheMongoRepository<Usuario> {

    public Uni<Usuario> findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public Multi<Usuario> findByNombre(String nombre) {
        return find("nombre like ?1", "%" + nombre + "%").stream();
    }

    public Multi<Usuario> findActivos() {
        return find("activo", true).stream();
    }

    public Uni<Long> countActivos() {
        return count("activo", true);
    }

    public Uni<Usuario> findByIdAndActivo(ObjectId id) {
        return find("_id = ?1 and activo = ?2", id, true).firstResult();
    }

    public Uni<Boolean> existsByEmail(String email) {
        return count("email", email)
                .map(count -> count > 0);
    }

    public Uni<Boolean> existsByEmailAndNotId(String email, ObjectId id) {
        return count("email = ?1 and _id != ?2", email, id)
                .map(count -> count > 0);
    }
}