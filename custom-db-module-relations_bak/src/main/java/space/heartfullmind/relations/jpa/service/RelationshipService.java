package space.heartfullmind.relations.jpa.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.heartfullmind.relations.data.RelationshipDTO;
import space.heartfullmind.relations.jpa.entity.RelationshipEntity;
import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.enums.RelationshipType;

import java.util.List;
import java.util.stream.Collectors;

public class RelationshipService {
    @PersistenceContext
    private EntityManagerFactory emf;

    private static final Logger log = LoggerFactory.getLogger(RelationshipService.class);

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void createRelationship(String userId, String relatedUserId, RelationshipType type, RelationshipStatus status) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            RelationshipEntity entity = new RelationshipEntity();
            entity.setUserId(userId);
            entity.setRelatedUserId(relatedUserId);
            entity.setRelationshipType(type);
            entity.setRelationshipStatus(status);
            em.persist(entity);
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

    public void updateRelationship(String userId, String relatedUserId, RelationshipType newType, RelationshipStatus status) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            RelationshipEntity entity = findRelationship(userId, relatedUserId);
            entity.setRelationshipType(newType);
            entity.setRelationshipStatus(status);
            em.merge(entity);
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

    public void deleteRelationship(String userId, String relatedUserId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            RelationshipEntity entity = findRelationship(userId, relatedUserId);
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

    public Object setRelationshipStatus(String userId, String relatedUserId, RelationshipStatus status, RelationshipType type) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            RelationshipEntity entity = findRelationship(userId, relatedUserId);
            entity.setRelationshipStatus(status);
            entity.setRelationshipType(type);
            Object result = em.merge(entity);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<RelationshipDTO> getRelationships(String userId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT r FROM RelationshipEntity r WHERE r.userId = :userId", RelationshipEntity.class)
                    .setParameter("userId", userId)
                    .getResultList()
                    .stream()
                    .map(entity -> RelationshipDTO.from(entity))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    private RelationshipEntity findRelationship(String userId, String relatedUserId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM RelationshipEntity r WHERE r.userId = :userId AND r.relatedUserId = :relatedUserId",
                            RelationshipEntity.class)
                    .setParameter("userId", userId)
                    .setParameter("relatedUserId", relatedUserId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}

