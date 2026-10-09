package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.ActivityLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActivityLogDaoImpl extends BaseDao<ActivityLog, Long> implements GenericDao<ActivityLog, Long> {

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        ActivityLog a = new ActivityLog();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setUsername(rs.getString("username"));
        a.setActionType(rs.getString("action_type"));
        a.setDetails(rs.getString("details"));
        a.setIpAddress(rs.getString("ip_address"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }

    @Override
    public Optional<ActivityLog> findById(Long id) {
        String sql = "SELECT * FROM activity_logs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find activity log: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ActivityLog> findAll() {
        return findRecent(50);
    }

    public List<ActivityLog> findRecent(int limit) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch activity logs: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public ActivityLog save(ActivityLog a) {
        String sql = "INSERT INTO activity_logs (user_id, username, action_type, details, ip_address, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                a.getUserId(),
                a.getUsername(),
                a.getActionType(),
                a.getDetails(),
                a.getIpAddress(),
                now
        );
        a.setId(id);
        a.setCreatedAt(now);
        return a;
    }

    @Override
    public boolean update(ActivityLog entity) {
        return false; // Logs are append-only
    }

    @Override
    public boolean deleteById(Long id) {
        return executeUpdate("DELETE FROM activity_logs WHERE id = ?", id) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM activity_logs";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count activity logs: " + e.getMessage(), e);
        }
        return 0;
    }
}
