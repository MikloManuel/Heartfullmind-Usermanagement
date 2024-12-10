package space.heartfullmind.relations.jpa.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.heartfullmind.relations.jpa.entity.FriendsRequestsEntity;
import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.enums.RelationshipType;
import space.heartfullmind.relations.rest.FriendsRequestsResource;


import java.util.List;
import java.util.UUID;

public class FriendsRequestsService {
    @PersistenceContext
    private EntityManagerFactory emf;

    private static final Logger log = LoggerFactory.getLogger(FriendsRequestsService.class);

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void createRequest(String userId, String relatedUserId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            FriendsRequestsEntity entity1 = new FriendsRequestsEntity();
            entity1.setUserId(userId);
            entity1.setRelatedUserId(relatedUserId);
            em.persist(entity1);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteRequest(String userId, String relatedUserId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            FriendsRequestsEntity entity = findRequests(userId, relatedUserId);
            em.remove(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<FriendsRequestsEntity> getPendingRequests(String userId) {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM FriendsRequestsEntity r WHERE r.userId = :userId OR r.relatedUserId = :userId",
                            FriendsRequestsEntity.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    private FriendsRequestsEntity findRequests(String userId, String relatedUserId) {
        try (EntityManager em = getEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM FriendsRequestsEntity r WHERE r.userId = :userId AND r.relatedUserId = :relatedUserId",
                            FriendsRequestsEntity.class)
                    .setParameter("userId", userId)
                    .setParameter("relatedUserId", relatedUserId)
                    .getSingleResult();
        }
    }
}

