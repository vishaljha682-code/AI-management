package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.ChatMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatMessageDaoImpl extends BaseDao<ChatMessage, Long> implements GenericDao<ChatMessage, Long> {

    private ChatMessage mapRow(ResultSet rs) throws SQLException {
        ChatMessage m = new ChatMessage();
        m.setId(rs.getLong("id"));
        m.setConversationId(rs.getString("conversation_id"));
        m.setUserId(rs.getLong("user_id"));
        m.setSenderRole(rs.getString("sender_role"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        return m;
    }

    @Override
    public Optional<ChatMessage> findById(Long id) {
        String sql = "SELECT * FROM chat_messages WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find chat message: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ChatMessage> findAll() {
        return findByConversationId(null);
    }

    public List<ChatMessage> findByConversationId(String convId) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = convId == null ?
                "SELECT * FROM chat_messages ORDER BY id ASC" :
                "SELECT * FROM chat_messages WHERE conversation_id = ? ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (convId != null) {
                ps.setString(1, convId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch messages: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public ChatMessage save(ChatMessage m) {
        String sql = "INSERT INTO chat_messages (conversation_id, user_id, sender_role, content, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                m.getConversationId(),
                m.getUserId(),
                m.getSenderRole(),
                m.getContent(),
                now
        );
        m.setId(id);
        m.setCreatedAt(now);
        return m;
    }

    @Override
    public boolean update(ChatMessage entity) {
        return false;
    }

    @Override
    public boolean deleteById(Long id) {
        return executeUpdate("DELETE FROM chat_messages WHERE id = ?", id) > 0;
    }

    public boolean deleteByConversationId(String convId) {
        return executeUpdate("DELETE FROM chat_messages WHERE conversation_id = ?", convId) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM chat_messages";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count messages: " + e.getMessage(), e);
        }
        return 0;
    }
}
