package com.example.health;

import com.mongodb.reactivestreams.client.MongoClient;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Readiness
@ApplicationScoped
public class MongoHealthCheck implements HealthCheck {

    @Inject
    MongoClient mongoClient;

    @Override
    public HealthCheckResponse call() {
        try {
            // Crear un CompletableFuture para manejar el resultado
            CompletableFuture<org.bson.Document> future = new CompletableFuture<>();

            // Ejecutar el comando ping
            mongoClient.getDatabase("admin")
                    .runCommand(org.bson.Document.parse("{ping: 1}"))
                    .subscribe(new org.reactivestreams.Subscriber<org.bson.Document>() {
                        @Override
                        public void onSubscribe(org.reactivestreams.Subscription s) {
                            s.request(1);
                        }

                        @Override
                        public void onNext(org.bson.Document document) {
                            future.complete(document);
                        }

                        @Override
                        public void onError(Throwable t) {
                            future.completeExceptionally(t);
                        }

                        @Override
                        public void onComplete() {
                            // Ya se completó en onNext
                        }
                    });

            // Esperar el resultado con timeout
            future.get(5, TimeUnit.SECONDS);

            return HealthCheckResponse.up("MongoDB connection health check");
        } catch (Exception e) {
            return HealthCheckResponse.down("MongoDB connection health check");
        }
    }
}