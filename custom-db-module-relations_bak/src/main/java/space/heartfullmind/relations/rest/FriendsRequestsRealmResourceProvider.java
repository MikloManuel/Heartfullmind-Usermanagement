package space.heartfullmind.relations.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class FriendsRequestsRealmResourceProvider implements RealmResourceProvider {
    private final KeycloakSession session;

    public FriendsRequestsRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        String path = session.getContext().getUri().getPath();
        if (path.contains("friends-requests")) {
            return new FriendsRequestsResource(session);
        } else if (path.contains("relationships")) {
            return new RelationshipResource(session);
        }
        return null;
    }

    public void close() {
    }
}
