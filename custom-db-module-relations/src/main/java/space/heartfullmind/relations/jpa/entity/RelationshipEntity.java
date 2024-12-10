package space.heartfullmind.relations.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.enums.RelationshipType;


import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "RELATIONSHIPS")
public class RelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "RELATED_USER_ID")
    private String relatedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "RELATIONSHIP_STATUS")
    private RelationshipStatus relationshipStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "RELATIONSHIP_TYPE")
    private RelationshipType relationshipType;

    @Column(name = "CREATED_AT")
    private Instant createdAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRelatedUserId() {
        return relatedUserId;
    }

    public void setRelatedUserId(String relatedUserId) {
        this.relatedUserId = relatedUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public RelationshipType getRelationshipType(RelationshipStatus pending) {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public RelationshipStatus getRelationshipStatus() {
        return relationshipStatus;
    }
    public void setRelationshipStatus(RelationshipStatus relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }
}
