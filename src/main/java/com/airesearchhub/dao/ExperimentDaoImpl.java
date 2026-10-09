package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.Experiment;
import com.airesearchhub.model.enums.AlgorithmType;
import com.airesearchhub.model.enums.TaskType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExperimentDaoImpl extends BaseDao<Experiment, Long> implements ExperimentDao {

    private Experiment mapRow(ResultSet rs) throws SQLException {
        Experiment e = new Experiment();
        e.setId(rs.getLong("id"));
        e.setExperimentName(rs.getString("experiment_name"));
        e.setTrainingJobId(rs.getLong("training_job_id"));
        e.setProjectId(rs.getLong("project_id"));
        e.setProjectName(rs.getString("project_name"));
        e.setDatasetId(rs.getLong("dataset_id"));
        e.setDatasetName(rs.getString("dataset_name"));
        e.setAlgorithm(AlgorithmType.fromString(rs.getString("algorithm")));
        e.setTaskType(TaskType.valueOf(rs.getString("task_type")));
        e.setHyperparametersJson(rs.getString("hyperparameters_json"));
        e.setMetricsJson(rs.getString("metrics_json"));
        e.setConfusionMatrixJson(rs.getString("confusion_matrix_json"));
        e.setFeatureImportanceJson(rs.getString("feature_importance_json"));
        e.setPredictionsSummaryJson(rs.getString("predictions_summary_json"));
        e.setPrimaryMetricValue(rs.getDouble("primary_metric_value"));
        e.setPrimaryMetricName(rs.getString("primary_metric_name"));
        e.setCreatedById(rs.getLong("created_by_id"));
        e.setCreatedByName(rs.getString("created_by_name"));
        e.setCreatedAt(rs.getTimestamp("created_at"));
        return e;
    }

    @Override
    public Optional<Experiment> findById(Long id) {
        String sql = "SELECT * FROM experiments WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find experiment: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Experiment> findAll() {
        List<Experiment> list = new ArrayList<>();
        String sql = "SELECT * FROM experiments ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all experiments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Experiment> findByProjectId(Long projectId) {
        List<Experiment> list = new ArrayList<>();
        String sql = "SELECT * FROM experiments WHERE project_id = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch experiments by project: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Experiment> findByDatasetId(Long datasetId) {
        List<Experiment> list = new ArrayList<>();
        String sql = "SELECT * FROM experiments WHERE dataset_id = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, datasetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch experiments by dataset: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Experiment> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        StringBuilder sb = new StringBuilder("SELECT * FROM experiments WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(i == 0 ? "?" : ", ?");
        }
        sb.append(")");
        List<Experiment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch experiments by IDs: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Experiment save(Experiment e) {
        String sql = "INSERT INTO experiments (experiment_name, training_job_id, project_id, project_name, dataset_id, dataset_name, algorithm, task_type, hyperparameters_json, metrics_json, confusion_matrix_json, feature_importance_json, predictions_summary_json, primary_metric_value, primary_metric_name, created_by_id, created_by_name, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                e.getExperimentName(),
                e.getTrainingJobId(),
                e.getProjectId(),
                e.getProjectName(),
                e.getDatasetId(),
                e.getDatasetName(),
                e.getAlgorithm().name(),
                e.getTaskType().name(),
                e.getHyperparametersJson(),
                e.getMetricsJson(),
                e.getConfusionMatrixJson(),
                e.getFeatureImportanceJson(),
                e.getPredictionsSummaryJson(),
                e.getPrimaryMetricValue(),
                e.getPrimaryMetricName(),
                e.getCreatedById(),
                e.getCreatedByName(),
                now
        );
        e.setId(id);
        e.setCreatedAt(now);
        return e;
    }

    @Override
    public boolean update(Experiment e) {
        String sql = "UPDATE experiments SET experiment_name = ? WHERE id = ?";
        return executeUpdate(sql, e.getExperimentName(), e.getId()) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return executeUpdate("DELETE FROM experiments WHERE id = ?", id) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM experiments";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count experiments: " + e.getMessage(), e);
        }
        return 0;
    }
}
