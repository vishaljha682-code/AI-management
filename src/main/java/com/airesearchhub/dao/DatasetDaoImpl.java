package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.Dataset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatasetDaoImpl extends BaseDao<Dataset, Long> implements DatasetDao {

    private Dataset mapRow(ResultSet rs) throws SQLException {
        Dataset d = new Dataset();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setDescription(rs.getString("description"));
        d.setFilename(rs.getString("filename"));
        d.setFilePath(rs.getString("file_path"));
        d.setFileSize(rs.getLong("file_size"));
        d.setRowCount(rs.getInt("row_count"));
        d.setColumnCount(rs.getInt("column_count"));
        d.setColumnsJson(rs.getString("columns_json"));
        d.setUploadedById(rs.getLong("uploaded_by_id"));
        d.setUploaderName(rs.getString("uploader_name"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        d.setUpdatedAt(rs.getTimestamp("updated_at"));
        return d;
    }

    @Override
    public Optional<Dataset> findById(Long id) {
        String sql = "SELECT * FROM datasets WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find dataset: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Dataset> findAll() {
        List<Dataset> list = new ArrayList<>();
        String sql = "SELECT * FROM datasets ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all datasets: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Dataset> findByUploadedById(Long userId) {
        List<Dataset> list = new ArrayList<>();
        String sql = "SELECT * FROM datasets WHERE uploaded_by_id = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch user datasets: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Dataset save(Dataset d) {
        String sql = "INSERT INTO datasets (name, description, filename, file_path, file_size, row_count, column_count, columns_json, uploaded_by_id, uploader_name, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                d.getName(),
                d.getDescription(),
                d.getFilename(),
                d.getFilePath(),
                d.getFileSize(),
                d.getRowCount(),
                d.getColumnCount(),
                d.getColumnsJson(),
                d.getUploadedById(),
                d.getUploaderName(),
                now,
                now
        );
        d.setId(id);
        d.setCreatedAt(now);
        d.setUpdatedAt(now);
        return d;
    }

    @Override
    public boolean update(Dataset d) {
        String sql = "UPDATE datasets SET name = ?, description = ?, row_count = ?, column_count = ?, columns_json = ?, updated_at = ? WHERE id = ?";
        return executeUpdate(sql,
                d.getName(),
                d.getDescription(),
                d.getRowCount(),
                d.getColumnCount(),
                d.getColumnsJson(),
                new Timestamp(System.currentTimeMillis()),
                d.getId()
        ) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return executeUpdate("DELETE FROM datasets WHERE id = ?", id) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM datasets";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count datasets: " + e.getMessage(), e);
        }
        return 0;
    }
}
