package com.airesearchhub.dto;

public class AssistantRequestDto {
    private String conversationId;
    private String message;
    private Long projectId;
    private Long datasetId;
    private String actionType; // "CHAT", "EXPLAIN_ALGORITHM", "SUGGEST_ALGORITHM", "CODE_STARTER", "SUMMARIZE_DOC", "EXPLAIN_EXPERIMENT"

    public AssistantRequestDto() {
        this.actionType = "CHAT";
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
