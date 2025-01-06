package org.keycloak.services.resources.account;

import org.keycloak.services.cors.Cors;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ClientModel;

public class CorsPreflightService {

    private final KeycloakSession session;
    private final ClientModel client;

    public CorsPreflightService(KeycloakSession session) {
        this.session = session;
        this.client = session.getContext().getClient();
    }

    @Path("{any:.*}")
    @OPTIONS
    public Response preflight() {
        return Cors.builder()
                .auth()
                .allowedOrigins(session, client)
                .allowedMethods("GET", "POST", "DELETE", "PUT", "HEAD", "OPTIONS")
                .preflight()
                .add(Response.ok()
                        .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                        .header("Access-Control-Allow-Origin", "*"));
    }
}

