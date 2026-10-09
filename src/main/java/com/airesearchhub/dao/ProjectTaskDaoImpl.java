package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.ProjectTask;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectTaskDaoImpl extends BaseDao<ProjectTask, Long> implements GenericDao<ProjectTask, Long> {

    private ProjectTask mapRow(ResultSet rs) throws SQLException {
        ProjectTask t = new ProjectTask();
        t.setId(rs.getLong("id"));
        t.setProjectId(rs.getLong("project_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setStatus(rs.getString("status"));
        t.setPriority(rs.getString("priority"));
        t.setAssignedToUserId(rs.getLong("assigned_to_user_id"));
        t.setAssignedToName(rs.getString("assigned_to_name"));
        t.setDueDate(rs.getDate("due_date"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }

    @Override
    public Optional<ProjectTask> findById(Long id) {
        String sql = "SELECT * FROM project_tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find task: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ProjectTask> findAll() {
        return findByProjectId(null);
    }

    public List<ProjectTask> findByProjectId(Long projectId) {
        List<ProjectTask> list = new ArrayList<>();
        String sql = projectId == null ?
                "SELECT * FROM project_tasks ORDER BY id DESC" :
                "SELECT * FROM project_tasks WHERE project_id = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (projectId != null) {
                ps.setLong(1, projectId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch project tasks: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public ProjectTask save(ProjectTask t) {
        String sql = "INSERT INTO project_tasks (project_id, title, description, status, priority, assigned_to_user_id, assigned_to_name, due_date, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                t.getProjectId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getAssignedToUserId(),
                t.getAssignedToName(),
                t.getDueDate(),
                now
        );
        t.setId(id);
        t.setCreatedAt(now);
        return t;
    }

    @Override
    public boolean update(ProjectTask t) {
        String sql = "UPDATE project_tasks SET title = ?, description = ?, status = ?, priority = ?, assigned_to_user_id = ?, assigned_to_name = ?, due_date = ? WHERE id = ?";
        return executeUpdate(sql,
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getAssignedToUserId(),
                t.getAssignedToName(),
                t.getDueDate(),
                t.getId()
        ) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return executeUpdate("DELETE FROM project_tasks WHERE id = ?", id) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM project_tasks";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count tasks: " + e.getMessage(), e);
        }
        return 0;
    }
}
