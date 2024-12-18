package space.heartfullmind.relations.jpa.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import space.heartfullmind.relations.data.RelationshipDTO;
import space.heartfullmind.relations.jpa.entity.RelationshipEntity;
import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.enums.RelationshipType;

import java.util.List;
import java.util.stream.Collectors;

public class RelationshipService {

    protected EntityManager em;
    final private KeycloakSession session;

    public RelationshipService(KeycloakSession session) {
        this.session = session;
    }

    public void createRelationship(String userId, String relatedUserId, RelationshipType type, RelationshipStatus status) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        // Create first direction
        RelationshipEntity entity1 = new RelationshipEntity();
        entity1.setUserId(userId);
        entity1.setRelatedUserId(relatedUserId);
        entity1.setRelationshipType(type);
        entity1.setRelationshipStatus(status);
        this.em.persist(entity1);

        // Create reverse direction
        RelationshipEntity entity2 = new RelationshipEntity();
        entity2.setUserId(relatedUserId);
        entity2.setRelatedUserId(userId);
        entity2.setRelationshipType(type);
        entity2.setRelationshipStatus(status);
        this.em.persist(entity2);
    }

    public void updateRelationship(String userId, String relatedUserId, RelationshipType newType, RelationshipStatus status) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        RelationshipEntity entity = findRelationship(userId, relatedUserId);
        entity.setRelationshipType(newType);
        entity.setRelationshipStatus(status);
        this.em.merge(entity);
    }




    public void deleteRelationship(String userId, String relatedUserId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();

        try {
            RelationshipEntity entity = findRelationship(userId, relatedUserId);
            this.em.remove(entity);
        } catch (NoResultException e) {
            // Entity not found - continue
        }

        try {
            RelationshipEntity reverseEntity = findRelationship(relatedUserId, userId);
            this.em.remove(reverseEntity);
        } catch (NoResultException e) {
            // Reverse entity not found - continue
        }
    }

    public List<RelationshipDTO> getRelationships(String userId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return this.em.createQuery(
                        "SELECT r FROM RelationshipEntity r WHERE " +
                                "r.userId = :userId OR r.relatedUserId = :userId",
                        RelationshipEntity.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(RelationshipDTO::from)
                .toList();
    }

    private RelationshipEntity findRelationship(String userId, String relatedUserId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return this.em.createQuery(
                        "SELECT r FROM RelationshipEntity r WHERE r.userId = :userId AND r.relatedUserId = :relatedUserId",
                        RelationshipEntity.class)
                .setParameter("userId", userId)
                .setParameter("relatedUserId", relatedUserId)
                .getSingleResult();
    }

    public boolean hasExistingRelationship(String userId, String relatedUserId) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return this.em.createQuery(
                            "SELECT r FROM RelationshipEntity r WHERE " +
                                    "(r.userId = :userId AND r.relatedUserId = :relatedUserId) OR " +
                                    "(r.userId = :relatedUserId AND r.relatedUserId = :userId) " +
                                    "AND r.relationshipStatus = :status",
                            RelationshipEntity.class)
                    .setParameter("userId", userId)
                    .setParameter("relatedUserId", relatedUserId)
                    .setParameter("status", RelationshipStatus.ACCEPTED)
                    .getResultList()
                    .isEmpty();
    }
}
