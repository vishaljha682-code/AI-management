package com.airesearchhub.dao;

import com.airesearchhub.model.entity.TrainingJob;
import com.airesearchhub.model.enums.JobStatus;
import java.util.List;

public interface TrainingJobDao extends GenericDao<TrainingJob, Long> {
    List<TrainingJob> findByProjectId(Long projectId);
    List<TrainingJob> findByCreatedById(Long userId);
    List<TrainingJob> findByStatus(JobStatus status);
    boolean updateStatusAndProgress(Long jobId, JobStatus status, int progress, String errorDetails);
}
