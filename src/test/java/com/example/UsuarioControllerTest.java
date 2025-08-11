package com.example;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class UsuarioControllerTest {

    @Test
    public void testObtenerTodosLosUsuarios() {
        given()
          .when().get("/api/usuarios")
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON);
    }

    @Test
    public void testCrearUsuario() {
        String usuarioJson = """
            {
                "nombre": "Juan Pérez",
                "email": "juan.perez@example.com",
                "telefono": "1234567890"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(usuarioJson)
          .when()
            .post("/api/usuarios")
          .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nombre", is("Juan Pérez"))
            .body("email", is("juan.perez@example.com"));
    }

    @Test
    public void testContarUsuarios() {
        given()
          .when().get("/api/usuarios/count")
          .then()
             .statusCode(200)
             .body("count", notNullValue())
             .body("timestamp", notNullValue());
    }
}