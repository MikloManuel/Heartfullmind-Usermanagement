package space.heartfullmind.relations.jpa.service;

import jakarta.persistence.EntityManager;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import space.heartfullmind.relations.jpa.entity.FriendsRequestsEntity;


import java.util.List;

public class FriendsRequestsService {

    private final KeycloakSession session;
    protected EntityManager em;

    public FriendsRequestsService(KeycloakSession session) {
        this.session = session;
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    public void createRequest(String userId, String relatedUserId) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        KeycloakTransaction transaction = session.getTransactionManager();

        if (!transaction.isActive()) {
            transaction.begin();
        }

        try {
            FriendsRequestsEntity entity = new FriendsRequestsEntity();
            entity.setUserId(userId);
            entity.setRelatedUserId(relatedUserId);
            em.persist(entity);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }



    public void deleteRequest(String userId, String relatedUserId) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        KeycloakTransaction transaction = session.getTransactionManager();

        if (!transaction.isActive()) {
            transaction.begin();
        }
        try {
            FriendsRequestsEntity entity = findRequests(userId, relatedUserId);
            em.remove(entity);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    public List<FriendsRequestsEntity> getPendingRequests(String userId) {
        List<FriendsRequestsEntity> list = em.createQuery("SELECT r FROM FriendsRequestsEntity r WHERE r.userId = :userId", FriendsRequestsEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        return list;
    }

    private FriendsRequestsEntity findRequests(String userId, String relatedUserId) {
        return em.createQuery(
                        "SELECT r FROM FriendsRequestsEntity r WHERE r.userId = :userId AND r.relatedUserId = :relatedUserId",
                        FriendsRequestsEntity.class)
                .setParameter("userId", userId)
                .setParameter("relatedUserId", relatedUserId)
                .getSingleResult();
    }
}
