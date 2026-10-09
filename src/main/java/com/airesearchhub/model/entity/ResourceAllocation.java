package com.airesearchhub.model.entity;

public class ResourceAllocation extends BaseEntity {
    private Long userId;
    private String username;
    private double allocatedComputeHours;
    private double usedComputeHours;
    private double allocatedStorageMb;
    private double usedStorageMb;

    public ResourceAllocation() {
        super();
        this.allocatedComputeHours = 100.0;
        this.usedComputeHours = 0.0;
        this.allocatedStorageMb = 2048.0;
        this.usedStorageMb = 0.0;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getAllocatedComputeHours() {
        return allocatedComputeHours;
    }

    public void setAllocatedComputeHours(double allocatedComputeHours) {
        this.allocatedComputeHours = allocatedComputeHours;
    }

    public double getUsedComputeHours() {
        return usedComputeHours;
    }

    public void setUsedComputeHours(double usedComputeHours) {
        this.usedComputeHours = usedComputeHours;
    }

    public double getAllocatedStorageMb() {
        return allocatedStorageMb;
    }

    public void setAllocatedStorageMb(double allocatedStorageMb) {
        this.allocatedStorageMb = allocatedStorageMb;
    }

    public double getUsedStorageMb() {
        return usedStorageMb;
    }

    public void setUsedStorageMb(double usedStorageMb) {
        this.usedStorageMb = usedStorageMb;
    }
}
