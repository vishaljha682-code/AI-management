package com.airesearchhub.model.entity;

public class ChatMessage extends BaseEntity {
    private String conversationId;
    private Long userId;
    private String senderRole; // user, assistant, system
    private String content;

    public ChatMessage() {
        super();
    }

    public ChatMessage(String conversationId, Long userId, String senderRole, String content) {
        super();
        this.conversationId = conversationId;
        this.userId = userId;
        this.senderRole = senderRole;
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
