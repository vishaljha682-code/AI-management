package com.airesearchhub.model.entity;

import java.sql.Timestamp;

public class ProjectMember extends BaseEntity {
    private Long projectId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String projectRole; // LEAD, RESEARCHER, CONTRIBUTOR
    private Timestamp joinedAt;

    public ProjectMember() {
        super();
        this.joinedAt = new Timestamp(System.currentTimeMillis());
        this.projectRole = "RESEARCHER";
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(String projectRole) {
        this.projectRole = projectRole;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}
