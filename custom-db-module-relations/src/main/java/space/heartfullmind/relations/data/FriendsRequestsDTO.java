package space.heartfullmind.relations.data;

import space.heartfullmind.relations.jpa.entity.FriendsRequestsEntity;

public class FriendsRequestsDTO {
    private String userId;
    private String relatedUserId;

    public static FriendsRequestsDTO from(FriendsRequestsEntity entity) {
        FriendsRequestsDTO dto = new FriendsRequestsDTO();
        dto.setUserId(entity.getUserId());
        dto.setRelatedUserId(entity.getRelatedUserId());
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
}

