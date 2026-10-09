package com.airesearchhub.service;

import com.airesearchhub.dao.DatasetDao;
import com.airesearchhub.dao.DatasetDaoImpl;
import com.airesearchhub.dao.ProjectDao;
import com.airesearchhub.dao.ProjectDaoImpl;
import com.airesearchhub.dao.TrainingJobDao;
import com.airesearchhub.dao.TrainingJobDaoImpl;
import com.airesearchhub.dto.TrainingRequestDto;
import com.airesearchhub.exception.ResourceNotFoundException;
import com.airesearchhub.exception.ValidationException;
import com.airesearchhub.ml.engine.ModelTrainingEngine;
import com.airesearchhub.model.entity.Dataset;
import com.airesearchhub.model.entity.Project;
import com.airesearchhub.model.entity.TrainingJob;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.model.enums.AlgorithmType;
import com.airesearchhub.model.enums.JobStatus;
import com.airesearchhub.model.enums.TaskType;
import com.google.gson.Gson;
import java.util.List;

public class TrainingService {
    private final TrainingJobDao jobDao;
    private final DatasetDao datasetDao;
    private final ProjectDao projectDao;
    private final Gson gson;

    public TrainingService() {
        this.jobDao = new TrainingJobDaoImpl();
        this.datasetDao = new DatasetDaoImpl();
        this.projectDao = new ProjectDaoImpl();
        this.gson = new Gson();
    }

    public List<TrainingJob> getAllJobs() {
        return jobDao.findAll();
    }

    public List<TrainingJob> getJobsByUser(Long userId) {
        return jobDao.findByCreatedById(userId);
    }

    public List<TrainingJob> getJobsByProject(Long projectId) {
        return jobDao.findByProjectId(projectId);
    }

    public TrainingJob getJobById(Long id) {
        return jobDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training job not found: " + id));
    }

    public TrainingJob startTrainingJob(TrainingRequestDto req, User user) {
        if (req.getDatasetId() == null) {
            throw new ValidationException("Please select a valid dataset.");
        }
        Dataset dataset = datasetDao.findById(req.getDatasetId())
                .orElseThrow(() -> new ValidationException("Selected dataset not found."));

        AlgorithmType algorithm = AlgorithmType.fromString(req.getAlgorithm());
        TaskType taskType = algorithm.getDefaultTaskType();
        if (req.getTaskType() != null && !req.getTaskType().isEmpty()) {
            try {
                taskType = TaskType.valueOf(req.getTaskType().toUpperCase());
            } catch (Exception ignored) {}
        }

        if (taskType != TaskType.CLUSTERING && (req.getTargetColumn() == null || req.getTargetColumn().trim().isEmpty())) {
            throw new ValidationException("Target column is required for supervised learning tasks.");
        }

        TrainingJob job = new TrainingJob();
        String jobName = req.getJobName() != null && !req.getJobName().trim().isEmpty() ?
                req.getJobName().trim() :
                algorithm.getDisplayName() + " Training (" + dataset.getName() + ")";
        job.setJobName(jobName);
        job.setDatasetId(dataset.getId());
        job.setDatasetName(dataset.getName());

        if (req.getProjectId() != null) {
            Project project = projectDao.findById(req.getProjectId()).orElse(null);
            if (project != null) {
                job.setProjectId(project.getId());
                job.setProjectName(project.getTitle());
            }
        }

        job.setAlgorithm(algorithm);
        job.setTaskType(taskType);
        job.setTargetColumn(req.getTargetColumn());
        job.setFeatureColumnsJson(gson.toJson(req.getFeatureColumns()));
        job.setTrainSplitRatio(req.getTrainSplitRatio() > 0 ? req.getTrainSplitRatio() : 0.8);
        job.setHyperparametersJson(gson.toJson(req.getHyperparameters()));
        job.setStatus(JobStatus.PENDING);
        job.setProgressPercentage(0);
        job.setCreatedById(user.getId());
        job.setCreatedByName(user.getFullName() != null ? user.getFullName() : user.getUsername());

        TrainingJob savedJob = jobDao.save(job);

        // Submit to asynchronous Multithreaded Engine
        ModelTrainingEngine.getInstance().submitJob(savedJob);

        return savedJob;
    }

    public boolean cancelJob(Long jobId) {
        boolean cancelled = ModelTrainingEngine.getInstance().cancelJob(jobId);
        jobDao.updateStatusAndProgress(jobId, JobStatus.CANCELLED, 0, "Cancelled by user.");
        return cancelled;
    }
}
