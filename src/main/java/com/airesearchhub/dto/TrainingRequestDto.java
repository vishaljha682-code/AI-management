package com.airesearchhub.dto;

import java.util.List;
import java.util.Map;

public class TrainingRequestDto {
    private String jobName;
    private Long projectId;
    private Long datasetId;
    private String algorithm;
    private String taskType;
    private String targetColumn;
    private List<String> featureColumns;
    private double trainSplitRatio;
    private Map<String, Object> hyperparameters;

    public TrainingRequestDto() {
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

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public List<String> getFeatureColumns() {
        return featureColumns;
    }

    public void setFeatureColumns(List<String> featureColumns) {
        this.featureColumns = featureColumns;
    }

    public double getTrainSplitRatio() {
        return trainSplitRatio;
    }

    public void setTrainSplitRatio(double trainSplitRatio) {
        this.trainSplitRatio = trainSplitRatio;
    }

    public Map<String, Object> getHyperparameters() {
        return hyperparameters;
    }

    public void setHyperparameters(Map<String, Object> hyperparameters) {
        this.hyperparameters = hyperparameters;
    }
}
