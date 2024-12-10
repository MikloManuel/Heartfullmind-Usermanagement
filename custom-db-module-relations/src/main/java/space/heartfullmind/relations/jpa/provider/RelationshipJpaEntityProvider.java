package space.heartfullmind.relations.jpa.provider;

import org.keycloak.authorization.model.Scope;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import space.heartfullmind.relations.jpa.entity.FriendsRequestsEntity;
import space.heartfullmind.relations.jpa.entity.RelationshipEntity;

import java.util.List;

public class RelationshipJpaEntityProvider implements JpaEntityProvider {
    public static final String ID = "custom-jpa-entity-provider";

    public JpaEntityProvider create(KeycloakSession session) {
        return new RelationshipJpaEntityProvider();
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/jpa-changelog-master-relations.xml";
    }

    @Override
    public List<Class<?>> getEntities() {
        return List.of(
                FriendsRequestsEntity.class,
                RelationshipEntity.class
        );
    }

    @Override
    public String getFactoryId() {
        return RelationshipJpaEntityProvider.ID;
    }

    public void init(Scope config) {}

    public void postInit(KeycloakSessionFactory factory) {
    }

    public void close() {}
}
