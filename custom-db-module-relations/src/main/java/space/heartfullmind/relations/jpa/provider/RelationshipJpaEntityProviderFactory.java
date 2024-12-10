package space.heartfullmind.relations.jpa.provider;


import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;


public class RelationshipJpaEntityProviderFactory implements JpaEntityProviderFactory {

    @Override
    public void init(Config.Scope config) {

    }

    public static final String ID = "custom-jpa-relations-provider";

    @Override
    public JpaEntityProvider create(KeycloakSession session) {
        return new RelationshipJpaEntityProvider();
    }


    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    public String getFactoryId() {
        return ID;
    }
}



