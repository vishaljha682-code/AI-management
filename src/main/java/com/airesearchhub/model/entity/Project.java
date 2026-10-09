package com.airesearchhub.model.entity;

import com.airesearchhub.model.enums.ProjectStatus;
import java.sql.Date;

public class Project extends BaseEntity {
    private String title;
    private String description;
    private ProjectStatus status;
    private Long ownerId;
    private String ownerName;
    private int memberCount;
    private Date deadline;

    public Project() {
        super();
        this.status = ProjectStatus.ACTIVE;
        this.memberCount = 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
}
