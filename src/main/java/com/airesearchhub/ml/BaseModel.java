package com.airesearchhub.ml;

import com.airesearchhub.exception.ModelTrainingException;
import com.airesearchhub.model.enums.TaskType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseModel abstract class demonstrating OOP Inheritance and reusable metric logic.
 */
public abstract class BaseModel implements MachineLearningModel {
    protected final String algorithmName;
    protected final TaskType taskType;
    protected boolean isFitted = false;

    public BaseModel(String algorithmName, TaskType taskType) {
        this.algorithmName = algorithmName;
        this.taskType = taskType;
    }

    @Override
    public String getAlgorithmName() {
        return algorithmName;
    }

    @Override
    public TaskType getTaskType() {
        return taskType;
    }

    protected void validateInput(double[][] X, double[] y) {
        if (X == null || X.length == 0) {
            throw new ModelTrainingException("Input feature matrix X cannot be null or empty.");
        }
        if (taskType != TaskType.CLUSTERING && (y == null || y.length != X.length)) {
            throw new ModelTrainingException("Target array y length must match feature matrix X rows.");
        }
    }

    protected void checkFitted() {
        if (!isFitted) {
            throw new ModelTrainingException("Model has not been trained yet. Call fit() first.");
        }
    }

    @Override
    public EvaluationMetrics evaluate(double[][] X_test, double[] y_test) {
        checkFitted();
        double[] predictions = predict(X_test);
        EvaluationMetrics metrics = new EvaluationMetrics();

        if (taskType == TaskType.REGRESSION) {
            int n = y_test.length;
            if (n == 0) return metrics;

            double sumAe = 0.0;
            double sumSe = 0.0;
            double sumY = 0.0;

            for (int i = 0; i < n; i++) {
                double diff = y_test[i] - predictions[i];
                sumAe += Math.abs(diff);
                sumSe += diff * diff;
                sumY += y_test[i];
            }

            double meanY = sumY / n;
            double ssTotal = 0.0;
            for (int i = 0; i < n; i++) {
                ssTotal += Math.pow(y_test[i] - meanY, 2);
            }

            double mae = sumAe / n;
            double mse = sumSe / n;
            double rmse = Math.sqrt(mse);
            double r2 = ssTotal == 0.0 ? 1.0 : 1.0 - (sumSe / ssTotal);

            metrics.setMae(round(mae));
            metrics.setMse(round(mse));
            metrics.setRmse(round(rmse));
            metrics.setR2Score(round(Math.max(-1.0, Math.min(1.0, r2))));

        } else if (taskType == TaskType.CLASSIFICATION) {
            int n = y_test.length;
            if (n == 0) return metrics;

            int correct = 0;
            int tp = 0, fp = 0, fn = 0, tn = 0;

            for (int i = 0; i < n; i++) {
                int actual = (int) Math.round(y_test[i]);
                int pred = (int) Math.round(predictions[i]);

                if (actual == pred) correct++;

                if (actual == 1 && pred == 1) tp++;
                else if (actual == 0 && pred == 1) fp++;
                else if (actual == 1 && pred == 0) fn++;
                else if (actual == 0 && pred == 0) tn++;
            }

            double accuracy = (double) correct / n;
            double precision = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0.0;
            double recall = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0.0;
            double f1 = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0.0;

            metrics.setAccuracy(round(accuracy));
            metrics.setPrecision(round(precision));
            metrics.setRecall(round(recall));
            metrics.setF1Score(round(f1));
            metrics.setConfusionMatrix(new int[][]{{tn, fp}, {fn, tp}});
        }

        return metrics;
    }

    protected double round(double val) {
        return Math.round(val * 1000.0) / 1000.0;
    }

    @Override
    public Map<String, Double> getFeatureImportance(List<String> featureNames) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (featureNames == null) return map;
        double weight = 1.0 / featureNames.size();
        for (String f : featureNames) {
            map.put(f, round(weight));
        }
        return map;
    }
}
