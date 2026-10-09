package com.airesearchhub.model.entity;

public class ActivityLog extends BaseEntity {
    private Long userId;
    private String username;
    private String actionType;
    private String details;
    private String ipAddress;

    public ActivityLog() {
        super();
    }

    public ActivityLog(Long userId, String username, String actionType, String details, String ipAddress) {
        super();
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
        this.details = details;
        this.ipAddress = ipAddress;
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
