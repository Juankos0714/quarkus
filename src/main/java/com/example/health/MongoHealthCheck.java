package com.example.health;

import com.mongodb.reactivestreams.client.MongoClient;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Readiness
@ApplicationScoped
public class MongoHealthCheck implements HealthCheck {

    @Inject
    MongoClient mongoClient;

    @Override
    public HealthCheckResponse call() {
        try {
            // Intentar hacer ping a MongoDB
            Uni.createFrom().publisher(
                mongoClient.getDatabase("admin")
                          .runCommand(org.bson.Document.parse("{ping: 1}"))
            ).await().atMost(java.time.Duration.ofSeconds(5));

            return HealthCheckResponse.up("MongoDB connection health check");
        } catch (Exception e) {
            return HealthCheckResponse.down("MongoDB connection health check");
        }
    }
}