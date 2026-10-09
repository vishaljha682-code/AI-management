package com.airesearchhub.model.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public abstract class BaseEntity implements Serializable {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public BaseEntity() {
        long now = System.currentTimeMillis();
        this.createdAt = new Timestamp(now);
        this.updatedAt = new Timestamp(now);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
