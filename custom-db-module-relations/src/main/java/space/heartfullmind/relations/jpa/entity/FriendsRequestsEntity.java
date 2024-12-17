package space.heartfullmind.relations.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "FRIENDS_REQUESTS")
public class FriendsRequestsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // Change to UUID
    @Column(name = "ID", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "RELATED_USER_ID")
    private String relatedUserId;

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
}
