package com.airesearchhub.ml.engine;

import com.airesearchhub.dao.DatasetDao;
import com.airesearchhub.dao.DatasetDaoImpl;
import com.airesearchhub.dao.ExperimentDao;
import com.airesearchhub.dao.ExperimentDaoImpl;
import com.airesearchhub.dao.TrainingJobDao;
import com.airesearchhub.dao.TrainingJobDaoImpl;
import com.airesearchhub.dto.TrainingRequestDto;
import com.airesearchhub.exception.ModelTrainingException;
import com.airesearchhub.ml.DataFrame;
import com.airesearchhub.ml.EvaluationMetrics;
import com.airesearchhub.ml.MachineLearningModel;
import com.airesearchhub.ml.algorithms.DecisionTreeModel;
import com.airesearchhub.ml.algorithms.KNNModel;
import com.airesearchhub.ml.algorithms.KMeansClusteringModel;
import com.airesearchhub.ml.algorithms.LinearRegressionModel;
import com.airesearchhub.ml.algorithms.LogisticRegressionModel;
import com.airesearchhub.ml.algorithms.RandomForestModel;
import com.airesearchhub.model.entity.Dataset;
import com.airesearchhub.model.entity.Experiment;
import com.airesearchhub.model.entity.TrainingJob;
import com.airesearchhub.model.enums.AlgorithmType;
import com.airesearchhub.model.enums.JobStatus;
import com.airesearchhub.model.enums.TaskType;
import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Callable task for multithreaded asynchronous ML model training.
 * Demonstrates Multithreading, Generics, and OOP Polymorphism.
 */
public class TrainingTask implements Callable<Experiment> {
    private final TrainingJob job;
    private final TrainingJobDao jobDao;
    private final ExperimentDao experimentDao;
    private final DatasetDao datasetDao;
    private final Gson gson;

    public TrainingTask(TrainingJob job) {
        this.job = job;
        this.jobDao = new TrainingJobDaoImpl();
        this.experimentDao = new ExperimentDaoImpl();
        this.datasetDao = new DatasetDaoImpl();
        this.gson = new Gson();
    }

    @Override
    public Experiment call() throws Exception {
        try {
            // Update job to RUNNING
            updateProgress(JobStatus.RUNNING, 10, null);

            // Step 1: Load Dataset from disk
            Dataset dataset = datasetDao.findById(job.getDatasetId())
                    .orElseThrow(() -> new ModelTrainingException("Dataset not found: ID " + job.getDatasetId()));

            File csvFile = new File(dataset.getFilePath());
            if (!csvFile.exists()) {
                throw new ModelTrainingException("Dataset file does not exist on disk: " + dataset.getFilePath());
            }

            DataFrame df = DataFrame.fromCsv(csvFile);
            updateProgress(JobStatus.RUNNING, 30, null);

            // Step 2: Parse Features and Target
            List<String> featureCols = parseJsonList(job.getFeatureColumnsJson());
            if (featureCols.isEmpty()) {
                // If not specified, pick all numeric columns except target
                featureCols = new ArrayList<>();
                for (String c : df.getColumnNames()) {
                    if (!c.equals(job.getTargetColumn()) && "NUMERIC".equals(df.getColumnTypes().get(c))) {
                        featureCols.add(c);
                    }
                }
            }

            if (featureCols.isEmpty()) {
                throw new ModelTrainingException("No numeric feature columns found for model training.");
            }

            // Step 3: Train-Test Split
            DataFrame[] splits = df.trainTestSplit(job.getTrainSplitRatio());
            DataFrame trainDf = splits[0];
            DataFrame testDf = splits[1];
            updateProgress(JobStatus.RUNNING, 50, null);

            double[][] X_train = trainDf.extractFeatures(featureCols);
            double[] y_train = job.getTargetColumn() != null ? trainDf.extractTarget(job.getTargetColumn()) : new double[trainDf.getRowCount()];

            double[][] X_test = testDf.extractFeatures(featureCols);
            double[] y_test = job.getTargetColumn() != null ? testDf.extractTarget(job.getTargetColumn()) : new double[testDf.getRowCount()];

            // Step 4: Polymorphic instantiation of Algorithm
            MachineLearningModel model = createModelInstance(job.getAlgorithm());

            Map<String, Object> hyperParams = new HashMap<>();
            if (job.getHyperparametersJson() != null && !job.getHyperparametersJson().isEmpty()) {
                try {
                    hyperParams = gson.fromJson(job.getHyperparametersJson(), Map.class);
                } catch (Exception ignored) {}
            }

            // Train model
            updateProgress(JobStatus.RUNNING, 75, null);
            Thread.sleep(300); // Simulate realistic computation workload
            model.fit(X_train, y_train, hyperParams);

            // Step 5: Evaluate model on test set
            updateProgress(JobStatus.RUNNING, 90, null);
            EvaluationMetrics metrics = model.evaluate(X_test, y_test);
            Map<String, Double> featureImportance = model.getFeatureImportance(featureCols);

            // Generate sample test predictions summary
            double[] testPreds = model.predict(X_test);
            List<Map<String, Object>> predictionsSummary = new ArrayList<>();
            int sampleCount = Math.min(10, testPreds.length);
            for (int i = 0; i < sampleCount; i++) {
                Map<String, Object> sample = new LinkedHashMap<>();
                sample.put("actual", y_test.length > i ? Math.round(y_test[i] * 100.0) / 100.0 : 0);
                sample.put("predicted", Math.round(testPreds[i] * 100.0) / 100.0);
                predictionsSummary.add(sample);
            }

            // Determine primary metric
            double primaryVal = 0.0;
            String primaryName = "Score";
            if (model.getTaskType() == TaskType.REGRESSION) {
                primaryVal = metrics.getR2Score() != null ? metrics.getR2Score() : 0.0;
                primaryName = "R² Score";
            } else if (model.getTaskType() == TaskType.CLASSIFICATION) {
                primaryVal = metrics.getAccuracy() != null ? metrics.getAccuracy() : 0.0;
                primaryName = "Accuracy";
            } else if (model.getTaskType() == TaskType.CLUSTERING) {
                primaryVal = metrics.getSilhouetteScore() != null ? metrics.getSilhouetteScore() : 0.0;
                primaryName = "Silhouette Score";
            }

            // Step 6: Create Experiment Entity
            Experiment exp = new Experiment();
            exp.setExperimentName(job.getJobName() + " - Run #" + System.currentTimeMillis() % 1000);
            exp.setTrainingJobId(job.getId());
            exp.setProjectId(job.getProjectId());
            exp.setProjectName(job.getProjectName());
            exp.setDatasetId(job.getDatasetId());
            exp.setDatasetName(job.getDatasetName());
            exp.setAlgorithm(job.getAlgorithm());
            exp.setTaskType(job.getTaskType());
            exp.setHyperparametersJson(job.getHyperparametersJson());
            exp.setMetricsJson(gson.toJson(metrics.toMap()));
            exp.setConfusionMatrixJson(gson.toJson(metrics.getConfusionMatrix()));
            exp.setFeatureImportanceJson(gson.toJson(featureImportance));
            exp.setPredictionsSummaryJson(gson.toJson(predictionsSummary));
            exp.setPrimaryMetricValue(primaryVal);
            exp.setPrimaryMetricName(primaryName);
            exp.setCreatedById(job.getCreatedById());
            exp.setCreatedByName(job.getCreatedByName());

            Experiment savedExp = experimentDao.save(exp);

            // Step 7: Update job to COMPLETED
            updateProgress(JobStatus.COMPLETED, 100, null);

            return savedExp;

        } catch (InterruptedException ie) {
            updateProgress(JobStatus.CANCELLED, job.getProgressPercentage(), "Job cancelled by user.");
            throw ie;
        } catch (Exception e) {
            updateProgress(JobStatus.FAILED, job.getProgressPercentage(), e.getMessage());
            throw e;
        }
    }

    private synchronized void updateProgress(JobStatus status, int progress, String errorMsg) {
        job.setStatus(status);
        job.setProgressPercentage(progress);
        if (errorMsg != null) job.setErrorDetails(errorMsg);
        jobDao.updateStatusAndProgress(job.getId(), status, progress, errorMsg);
    }

    private MachineLearningModel createModelInstance(AlgorithmType algorithm) {
        switch (algorithm) {
            case LINEAR_REGRESSION:
                return new LinearRegressionModel();
            case LOGISTIC_REGRESSION:
                return new LogisticRegressionModel();
            case DECISION_TREE:
                return new DecisionTreeModel();
            case RANDOM_FOREST:
                return new RandomForestModel();
            case KNN:
                return new KNNModel();
            case KMEANS:
                return new KMeansClusteringModel();
            default:
                throw new ModelTrainingException("Unsupported algorithm: " + algorithm);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseJsonList(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            return gson.fromJson(json, List.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
