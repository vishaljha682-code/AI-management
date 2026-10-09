package com.airesearchhub.model.entity;

import com.airesearchhub.model.enums.AlgorithmType;
import com.airesearchhub.model.enums.JobStatus;
import com.airesearchhub.model.enums.TaskType;
import java.sql.Timestamp;

public class TrainingJob extends BaseEntity {
    private String jobName;
    private Long projectId;
    private String projectName;
    private Long datasetId;
    private String datasetName;
    private AlgorithmType algorithm;
    private TaskType taskType;
    private String hyperparametersJson;
    private String targetColumn;
    private String featureColumnsJson;
    private double trainSplitRatio;
    private JobStatus status;
    private int progressPercentage;
    private Timestamp startedAt;
    private Timestamp completedAt;
    private String errorDetails;
    private Long createdById;
    private String createdByName;

    public TrainingJob() {
        super();
        this.status = JobStatus.PENDING;
        this.progressPercentage = 0;
        this.trainSplitRatio = 0.8;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmType algorithm) {
        this.algorithm = algorithm;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getHyperparametersJson() {
        return hyperparametersJson;
    }

    public void setHyperparametersJson(String hyperparametersJson) {
        this.hyperparametersJson = hyperparametersJson;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public String getFeatureColumnsJson() {
        return featureColumnsJson;
    }

    public void setFeatureColumnsJson(String featureColumnsJson) {
        this.featureColumnsJson = featureColumnsJson;
    }

    public double getTrainSplitRatio() {
        return trainSplitRatio;
    }

    public void setTrainSplitRatio(double trainSplitRatio) {
        this.trainSplitRatio = trainSplitRatio;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
}
