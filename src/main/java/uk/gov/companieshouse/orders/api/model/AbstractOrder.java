package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * Factors out those data fields common to both {@link Checkout} and {@link Order}.
 */
public abstract class AbstractOrder implements TimestampedEntity {
    @Id
    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
