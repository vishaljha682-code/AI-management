package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.ResourceAllocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceDaoImpl extends BaseDao<ResourceAllocation, Long> implements GenericDao<ResourceAllocation, Long> {

    private ResourceAllocation mapRow(ResultSet rs) throws SQLException {
        ResourceAllocation r = new ResourceAllocation();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setUsername(rs.getString("username"));
        r.setAllocatedComputeHours(rs.getDouble("allocated_compute_hours"));
        r.setUsedComputeHours(rs.getDouble("used_compute_hours"));
        r.setAllocatedStorageMb(rs.getDouble("allocated_storage_mb"));
        r.setUsedStorageMb(rs.getDouble("used_storage_mb"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }

    @Override
    public Optional<ResourceAllocation> findById(Long id) {
        String sql = "SELECT * FROM resource_allocations WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find resource allocation: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<ResourceAllocation> findByUserId(Long userId) {
        String sql = "SELECT * FROM resource_allocations WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find resource allocation by user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ResourceAllocation> findAll() {
        List<ResourceAllocation> list = new ArrayList<>();
        String sql = "SELECT * FROM resource_allocations ORDER BY id ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all resource allocations: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public ResourceAllocation save(ResourceAllocation r) {
        String sql = "INSERT INTO resource_allocations (user_id, username, allocated_compute_hours, used_compute_hours, allocated_storage_mb, used_storage_mb, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                r.getUserId(),
                r.getUsername(),
                r.getAllocatedComputeHours(),
                r.getUsedComputeHours(),
                r.getAllocatedStorageMb(),
                r.getUsedStorageMb(),
                now
        );
        r.setId(id);
        r.setUpdatedAt(now);
        return r;
    }

    @Override
    public boolean update(ResourceAllocation r) {
        String sql = "UPDATE resource_allocations SET allocated_compute_hours = ?, used_compute_hours = ?, allocated_storage_mb = ?, used_storage_mb = ?, updated_at = ? WHERE id = ?";
        return executeUpdate(sql,
                r.getAllocatedComputeHours(),
                r.getUsedComputeHours(),
                r.getAllocatedStorageMb(),
                r.getUsedStorageMb(),
                new Timestamp(System.currentTimeMillis()),
                r.getId()
        ) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return executeUpdate("DELETE FROM resource_allocations WHERE id = ?", id) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM resource_allocations";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count allocations: " + e.getMessage(), e);
        }
        return 0;
    }
}
