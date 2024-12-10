package space.heartfullmind.relations.jpa.service;

import jakarta.persistence.EntityManager;
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

    private KeycloakSession session;

    public RelationshipService(KeycloakSession session) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        this.session = session;
    }

    public void createRelationship(String userId, String relatedUserId, RelationshipType type, RelationshipStatus status) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        KeycloakTransaction transaction = session.getTransactionManager();

        if (!transaction.isActive()) {
            transaction.begin();
        }

        try {
            RelationshipEntity entity = new RelationshipEntity();
            entity.setUserId(userId);
            entity.setRelatedUserId(relatedUserId);
            entity.setRelationshipType(type);
            entity.setRelationshipStatus(status);
            em.persist(entity);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }




    public void updateRelationship(String userId, String relatedUserId, RelationshipType newType, RelationshipStatus status) {
        RelationshipEntity entity = findRelationship(userId, relatedUserId);
        entity.setRelationshipType(newType);
        entity.setRelationshipStatus(status);
        em.merge(entity);
    }

    public void deleteRelationship(String userId, String relatedUserId) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        KeycloakTransaction transaction = session.getTransactionManager();

        if (!transaction.isActive()) {
            transaction.begin();
        }

        try {
            RelationshipEntity entity = findRelationship(userId, relatedUserId);
            em.remove(entity);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }

    }

    public List<RelationshipDTO> getRelationships(String userId) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return em.createQuery("SELECT r FROM RelationshipEntity r WHERE r.userId = :userId", RelationshipEntity.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(entity -> RelationshipDTO.from(entity))
                .collect(Collectors.toList());
    }


    private RelationshipEntity findRelationship(String userId, String relatedUserId) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        KeycloakTransaction transaction = session.getTransactionManager();

        if (!transaction.isActive()) {
            transaction.begin();
        }

        try {
            return em.createQuery(
                        "SELECT r FROM RelationshipEntity r WHERE r.userId = :userId AND r.relatedUserId = :relatedUserId",
                        RelationshipEntity.class)
                .setParameter("userId", userId)
                .setParameter("relatedUserId", relatedUserId)
                .getSingleResult();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }
}
