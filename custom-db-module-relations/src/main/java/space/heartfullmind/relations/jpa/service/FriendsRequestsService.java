package space.heartfullmind.relations.jpa.service;

import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import space.heartfullmind.relations.jpa.entity.FriendsRequestsEntity;
import space.heartfullmind.relations.rest.FriendsRequestsResource;


import java.util.List;

public class FriendsRequestsService {

    private final KeycloakSession session;
    protected EntityManager em;
    private static final Logger log = Logger.getLogger(FriendsRequestsResource.class);

    public FriendsRequestsService(KeycloakSession session) {
        this.session = session;
    }

    public void createRequest(String userId, String relatedUserId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        FriendsRequestsEntity entity = new FriendsRequestsEntity();
        entity.setUserId(userId);
        entity.setRelatedUserId(relatedUserId);
        this.em.persist(entity);
    }



    public void deleteRequest(String userId, String relatedUserId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        FriendsRequestsEntity entity = findRequests(userId, relatedUserId);
        this.em.remove(entity);
    }


    public List<FriendsRequestsEntity> getPendingRequests(String userId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        List<FriendsRequestsEntity> requests = this.em.createQuery(
                        "SELECT r FROM FriendsRequestsEntity r WHERE r.userId = :userId OR r.relatedUserId = :userId",
                        FriendsRequestsEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        return requests;
    }


    private FriendsRequestsEntity findRequests(String userId, String relatedUserId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return this.em.createQuery(
                        "SELECT r FROM FriendsRequestsEntity r WHERE r.userId = :userId AND r.relatedUserId = :relatedUserId",
                        FriendsRequestsEntity.class)
                .setParameter("userId", userId)
                .setParameter("relatedUserId", relatedUserId)
                .getSingleResult();
    }
}
