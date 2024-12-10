package space.heartfullmind.relations.data;

import space.heartfullmind.relations.jpa.enums.RelationshipStatus;
import space.heartfullmind.relations.jpa.entity.RelationshipEntity;
import space.heartfullmind.relations.jpa.enums.RelationshipType;

import java.time.Instant;
import java.util.UUID;

public class RelationshipDTO {
    private UUID id;
    private String userId;
    private String relatedUserId;
    private RelationshipStatus relationshipStatus;
    private RelationshipType relationshipType;
    private Instant createdAt;

    public static RelationshipDTO from(RelationshipEntity entity) {
        RelationshipDTO dto = new RelationshipDTO();
        dto.setUserId(entity.getUserId());
        dto.setRelatedUserId(entity.getRelatedUserId());
        dto.setRelationshipStatus(entity.getRelationshipStatus());
        dto.setRelationshipType(entity.getRelationshipType(RelationshipStatus.PENDING));
        return dto;
    }

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

    public RelationshipStatus getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(RelationshipStatus relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }
}
