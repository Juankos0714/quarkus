package com.example.controller;

import com.example.model.Usuario;
import com.example.service.UsuarioService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuarios", description = "Operaciones CRUD para usuarios")
public class UsuarioController {

    private static final Logger LOG = Logger.getLogger(UsuarioController.class);

    @Inject
    UsuarioService usuarioService;

    @GET
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna la lista completa de usuarios activos")
    @APIResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class)))
    public Multi<Usuario> obtenerTodos() {
        LOG.debug("GET /api/usuarios - Obteniendo todos los usuarios");
        return usuarioService.obtenerTodos();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico por su ID")
    @APIResponse(responseCode = "200", description = "Usuario encontrado",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class)))
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    @APIResponse(responseCode = "400", description = "ID inválido")
    public Uni<Response> obtenerPorId(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("id") String id) {
        
        LOG.debugf("GET /api/usuarios/%s - Obteniendo usuario por ID", id);
        
        return usuarioService.obtenerPorId(id)
                .map(usuario -> Response.ok(usuario).build())
                .onFailure(IllegalArgumentException.class)
                .recoverWithItem(throwable -> 
                    Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErrorResponse(throwable.getMessage())).build())
                .onFailure(RuntimeException.class)
                .recoverWithItem(throwable -> 
                    Response.status(Response.Status.NOT_FOUND)
                           .entity(new ErrorResponse(throwable.getMessage())).build());
    }

    @POST
    @Operation(summary = "Crear nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    @APIResponse(responseCode = "201", description = "Usuario creado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class)))
    @APIResponse(responseCode = "400", description = "Datos de usuario inválidos")
    @APIResponse(responseCode = "409", description = "El email ya existe")
    public Uni<Response> crear(@Valid Usuario usuario) {
        LOG.debugf("POST /api/usuarios - Creando usuario: %s", usuario.email);
        
        return usuarioService.crear(usuario)
                .map(usuarioCreado -> Response.status(Response.Status.CREATED)
                                            .entity(usuarioCreado).build())
                .onFailure(IllegalArgumentException.class)
                .recoverWithItem(throwable -> 
                    Response.status(Response.Status.CONFLICT)
                           .entity(new ErrorResponse(throwable.getMessage())).build())
                .onFailure()
                .recoverWithItem(throwable -> 
                    Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErrorResponse("Error al crear usuario")).build());
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    @APIResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class)))
    @APIResponse(responseCode = "400", description = "Datos inválidos")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    @APIResponse(responseCode = "409", description = "El email ya existe")
    public Uni<Response> actualizar(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("id") String id,
            @Valid Usuario usuario) {
        
        LOG.debugf("PUT /api/usuarios/%s - Actualizando usuario", id);
        
        return usuarioService.actualizar(id, usuario)
                .map(usuarioActualizado -> Response.ok(usuarioActualizado).build())
                .onFailure(IllegalArgumentException.class)
                .recoverWithItem(throwable -> {
                    if (throwable.getMessage().contains("ID")) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                     .entity(new ErrorResponse(throwable.getMessage())).build();
                    } else {
                        return Response.status(Response.Status.CONFLICT)
                                     .entity(new ErrorResponse(throwable.getMessage())).build();
                    }
                })
                .onFailure(RuntimeException.class)
                .recoverWithItem(throwable -> 
                    Response.status(Response.Status.NOT_FOUND)
                           .entity(new ErrorResponse(throwable.getMessage())).build());
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina (desactiva) un usuario del sistema")
    @APIResponse(responseCode = "200", description = "Usuario eliminado exitosamente")
    @APIResponse(responseCode = "400", description = "ID inválido")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> eliminar(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("id") String id) {
        
        LOG.debugf("DELETE /api/usuarios/%s - Eliminando usuario", id);
        
        return usuarioService.eliminar(id)
                .map(eliminado -> Response.ok(new SuccessResponse("Usuario eliminado exitosamente")).build())
                .onFailure(IllegalArgumentException.class)
                .recoverWithItem(throwable -> 
                    Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErrorResponse(throwable.getMessage())).build())
                .onFailure(RuntimeException.class)
                .recoverWithItem(throwable -> 
                    Response.status(Response.Status.NOT_FOUND)
                           .entity(new ErrorResponse(throwable.getMessage())).build());
    }

    @GET
    @Path("/buscar")
    @Operation(summary = "Buscar usuarios por nombre", description = "Busca usuarios que contengan el texto en el nombre")
    @APIResponse(responseCode = "200", description = "Búsqueda completada",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class)))
    public Multi<Usuario> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre", required = true)
            @QueryParam("nombre") String nombre) {
        
        LOG.debugf("GET /api/usuarios/buscar?nombre=%s - Buscando usuarios", nombre);
        
        if (nombre == null || nombre.trim().isEmpty()) {
            return Multi.createFrom().failure(new BadRequestException("El parámetro 'nombre' es obligatorio"));
        }
        
        return usuarioService.buscarPorNombre(nombre.trim());
    }

    @GET
    @Path("/count")
    @Operation(summary = "Contar usuarios", description = "Retorna el número total de usuarios activos")
    @APIResponse(responseCode = "200", description = "Conteo obtenido exitosamente")
    public Uni<Response> contarUsuarios() {
        LOG.debug("GET /api/usuarios/count - Contando usuarios");
        
        return usuarioService.contarUsuarios()
                .map(count -> Response.ok(new CountResponse(count)).build());
    }

    // Clases auxiliares para las respuestas
    public static class ErrorResponse {
        public String error;
        public long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class SuccessResponse {
        public String message;
        public long timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class CountResponse {
        public long count;
        public long timestamp;

        public CountResponse(long count) {
            this.count = count;
            this.timestamp = System.currentTimeMillis();
        }
    }
}