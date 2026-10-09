package com.airesearchhub.dao;

import com.airesearchhub.exception.DatabaseException;
import com.airesearchhub.model.entity.TrainingJob;
import com.airesearchhub.model.enums.AlgorithmType;
import com.airesearchhub.model.enums.JobStatus;
import com.airesearchhub.model.enums.TaskType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainingJobDaoImpl extends BaseDao<TrainingJob, Long> implements TrainingJobDao {

    private TrainingJob mapRow(ResultSet rs) throws SQLException {
        TrainingJob j = new TrainingJob();
        j.setId(rs.getLong("id"));
        j.setJobName(rs.getString("job_name"));
        j.setProjectId(rs.getLong("project_id"));
        j.setProjectName(rs.getString("project_name"));
        j.setDatasetId(rs.getLong("dataset_id"));
        j.setDatasetName(rs.getString("dataset_name"));
        j.setAlgorithm(AlgorithmType.fromString(rs.getString("algorithm")));
        j.setTaskType(TaskType.valueOf(rs.getString("task_type")));
        j.setHyperparametersJson(rs.getString("hyperparameters_json"));
        j.setTargetColumn(rs.getString("target_column"));
        j.setFeatureColumnsJson(rs.getString("feature_columns_json"));
        j.setTrainSplitRatio(rs.getDouble("train_split_ratio"));
        j.setStatus(JobStatus.valueOf(rs.getString("status")));
        j.setProgressPercentage(rs.getInt("progress_percentage"));
        j.setStartedAt(rs.getTimestamp("started_at"));
        j.setCompletedAt(rs.getTimestamp("completed_at"));
        j.setErrorDetails(rs.getString("error_details"));
        j.setCreatedById(rs.getLong("created_by_id"));
        j.setCreatedByName(rs.getString("created_by_name"));
        j.setCreatedAt(rs.getTimestamp("created_at"));
        return j;
    }

    @Override
    public Optional<TrainingJob> findById(Long id) {
        String sql = "SELECT * FROM training_jobs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find training job: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<TrainingJob> findAll() {
        List<TrainingJob> list = new ArrayList<>();
        String sql = "SELECT * FROM training_jobs ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all training jobs: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingJob> findByProjectId(Long projectId) {
        List<TrainingJob> list = new ArrayList<>();
        String sql = "SELECT * FROM training_jobs WHERE project_id = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch training jobs by project: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingJob> findByCreatedById(Long userId) {
        List<TrainingJob> list = new ArrayList<>();
        String sql = "SELECT * FROM training_jobs WHERE created_by_id = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch training jobs by user: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingJob> findByStatus(JobStatus status) {
        List<TrainingJob> list = new ArrayList<>();
        String sql = "SELECT * FROM training_jobs WHERE status = ? ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch jobs by status: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public TrainingJob save(TrainingJob j) {
        String sql = "INSERT INTO training_jobs (job_name, project_id, project_name, dataset_id, dataset_name, algorithm, task_type, hyperparameters_json, target_column, feature_columns_json, train_split_ratio, status, progress_percentage, started_at, completed_at, error_details, created_by_id, created_by_name, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long id = executeInsertWithGeneratedKey(sql,
                j.getJobName(),
                j.getProjectId(),
                j.getProjectName(),
                j.getDatasetId(),
                j.getDatasetName(),
                j.getAlgorithm().name(),
                j.getTaskType().name(),
                j.getHyperparametersJson(),
                j.getTargetColumn(),
                j.getFeatureColumnsJson(),
                j.getTrainSplitRatio(),
                j.getStatus().name(),
                j.getProgressPercentage(),
                j.getStartedAt(),
                j.getCompletedAt(),
                j.getErrorDetails(),
                j.getCreatedById(),
                j.getCreatedByName(),
                now
        );
        j.setId(id);
        j.setCreatedAt(now);
        return j;
    }

    @Override
    public boolean update(TrainingJob j) {
        String sql = "UPDATE training_jobs SET status = ?, progress_percentage = ?, started_at = ?, completed_at = ?, error_details = ? WHERE id = ?";
        return executeUpdate(sql,
                j.getStatus().name(),
                j.getProgressPercentage(),
                j.getStartedAt(),
                j.getCompletedAt(),
                j.getErrorDetails(),
                j.getId()
        ) > 0;
    }

    @Override
    public boolean updateStatusAndProgress(Long jobId, JobStatus status, int progress, String errorDetails) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String sql;
        if (status == JobStatus.RUNNING) {
            sql = "UPDATE training_jobs SET status = ?, progress_percentage = ?, started_at = COALESCE(started_at, ?) WHERE id = ?";
            return executeUpdate(sql, status.name(), progress, now, jobId) > 0;
        } else if (status == JobStatus.COMPLETED || status == JobStatus.FAILED || status == JobStatus.CANCELLED) {
            sql = "UPDATE training_jobs SET status = ?, progress_percentage = ?, completed_at = ?, error_details = ? WHERE id = ?";
            return executeUpdate(sql, status.name(), progress, now, errorDetails, jobId) > 0;
        } else {
            sql = "UPDATE training_jobs SET status = ?, progress_percentage = ? WHERE id = ?";
            return executeUpdate(sql, status.name(), progress, jobId) > 0;
        }
    }

    @Override
    public boolean deleteById(Long id) {
        return executeUpdate("DELETE FROM training_jobs WHERE id = ?", id) > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM training_jobs";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count jobs: " + e.getMessage(), e);
        }
        return 0;
    }
}
