package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.Project;
import com.airesearchhub.model.entity.ProjectMember;
import com.airesearchhub.model.enums.ProjectStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDaoImpl extends BaseDao<Project, Long> implements ProjectDao {

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getLong("id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setStatus(ProjectStatus.fromString(rs.getString("status")));
        p.setOwnerId(rs.getLong("owner_id"));
        p.setOwnerName(rs.getString("owner_name"));
        p.setMemberCount(rs.getInt("member_count"));
        p.setDeadline(rs.getDate("deadline"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }

    @Override
    public Optional<Project> findById(Long id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find project by id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Project> findAll() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find all projects: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Project> findByUserId(Long userId) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM projects p " +
                "LEFT JOIN project_members pm ON p.id = pm.project_id " +
                "WHERE p.owner_id = ? OR pm.user_id = ? " +
                "ORDER BY p.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find projects for user: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Project save(Project project) {
        String sql = "INSERT INTO projects (title, description, status, owner_id, owner_name, member_count, deadline, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                project.getTitle(),
                project.getDescription(),
                project.getStatus().name(),
                project.getOwnerId(),
                project.getOwnerName(),
                project.getMemberCount(),
                project.getDeadline(),
                now,
                now
        );
        project.setId(id);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        // Add owner as Lead member automatically
        ProjectMember pm = new ProjectMember();
        pm.setProjectId(id);
        pm.setUserId(project.getOwnerId());
        pm.setUserName(project.getOwnerName());
        pm.setProjectRole("LEAD");
        addMember(pm);

        return project;
    }

    @Override
    public boolean update(Project project) {
        String sql = "UPDATE projects SET title = ?, description = ?, status = ?, deadline = ?, member_count = ?, updated_at = ? WHERE id = ?";
        int updated = executeUpdate(sql,
                project.getTitle(),
                project.getDescription(),
                project.getStatus().name(),
                project.getDeadline(),
                project.getMemberCount(),
                new Timestamp(System.currentTimeMillis()),
                project.getId()
        );
        return updated > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        executeUpdate("DELETE FROM project_members WHERE project_id = ?", id);
        executeUpdate("DELETE FROM project_tasks WHERE project_id = ?", id);
        return executeUpdate("DELETE FROM projects WHERE id = ?", id) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM projects";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count projects: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<ProjectMember> findMembersByProjectId(Long projectId) {
        List<ProjectMember> list = new ArrayList<>();
        String sql = "SELECT * FROM project_members WHERE project_id = ? ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProjectMember pm = new ProjectMember();
                    pm.setId(rs.getLong("id"));
                    pm.setProjectId(rs.getLong("project_id"));
                    pm.setUserId(rs.getLong("user_id"));
                    pm.setUserName(rs.getString("user_name"));
                    pm.setUserEmail(rs.getString("user_email"));
                    pm.setProjectRole(rs.getString("project_role"));
                    pm.setJoinedAt(rs.getTimestamp("joined_at"));
                    list.add(pm);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find project members: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean addMember(ProjectMember member) {
        String sql = "INSERT INTO project_members (project_id, user_id, user_name, user_email, project_role, joined_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Long id = executeInsertWithGeneratedKey(sql,
                member.getProjectId(),
                member.getUserId(),
                member.getUserName(),
                member.getUserEmail(),
                member.getProjectRole(),
                new Timestamp(System.currentTimeMillis())
        );
        if (id != null) {
            member.setId(id);
            // update member count
            executeUpdate("UPDATE projects SET member_count = (SELECT COUNT(*) FROM project_members WHERE project_id = ?) WHERE id = ?",
                    member.getProjectId(), member.getProjectId());
            return true;
        }
        return false;
    }

    @Override
    public boolean removeMember(Long projectId, Long userId) {
        int removed = executeUpdate("DELETE FROM project_members WHERE project_id = ? AND user_id = ?", projectId, userId);
        if (removed > 0) {
            executeUpdate("UPDATE projects SET member_count = (SELECT COUNT(*) FROM project_members WHERE project_id = ?) WHERE id = ?",
                    projectId, projectId);
            return true;
        }
        return false;
    }
}
