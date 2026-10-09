package com.airesearchhub.model.entity;

import com.airesearchhub.model.enums.AlgorithmType;
import com.airesearchhub.model.enums.TaskType;

public class Experiment extends BaseEntity {
    private String experimentName;
    private Long trainingJobId;
    private Long projectId;
    private String projectName;
    private Long datasetId;
    private String datasetName;
    private AlgorithmType algorithm;
    private TaskType taskType;
    private String hyperparametersJson;
    private String metricsJson;
    private String confusionMatrixJson;
    private String featureImportanceJson;
    private String predictionsSummaryJson;
    private double primaryMetricValue; // e.g. Accuracy or R^2 or Silhouette
    private String primaryMetricName;
    private Long createdById;
    private String createdByName;

    public Experiment() {
        super();
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public Long getTrainingJobId() {
        return trainingJobId;
    }

    public void setTrainingJobId(Long trainingJobId) {
        this.trainingJobId = trainingJobId;
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

    public String getMetricsJson() {
        return metricsJson;
    }

    public void setMetricsJson(String metricsJson) {
        this.metricsJson = metricsJson;
    }

    public String getConfusionMatrixJson() {
        return confusionMatrixJson;
    }

    public void setConfusionMatrixJson(String confusionMatrixJson) {
        this.confusionMatrixJson = confusionMatrixJson;
    }

    public String getFeatureImportanceJson() {
        return featureImportanceJson;
    }

    public void setFeatureImportanceJson(String featureImportanceJson) {
        this.featureImportanceJson = featureImportanceJson;
    }

    public String getPredictionsSummaryJson() {
        return predictionsSummaryJson;
    }

    public void setPredictionsSummaryJson(String predictionsSummaryJson) {
        this.predictionsSummaryJson = predictionsSummaryJson;
    }

    public double getPrimaryMetricValue() {
        return primaryMetricValue;
    }

    public void setPrimaryMetricValue(double primaryMetricValue) {
        this.primaryMetricValue = primaryMetricValue;
    }

    public String getPrimaryMetricName() {
        return primaryMetricName;
    }

    public void setPrimaryMetricName(String primaryMetricName) {
        this.primaryMetricName = primaryMetricName;
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
