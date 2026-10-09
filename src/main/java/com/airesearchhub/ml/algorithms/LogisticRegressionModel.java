package com.airesearchhub.ml.algorithms;

import com.airesearchhub.ml.BaseModel;
import com.airesearchhub.model.enums.TaskType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Logistic Regression Model for classification tasks.
 */
public class LogisticRegressionModel extends BaseModel {
    private double[] weights;
    private double bias;
    private double[] featureMeans;
    private double[] featureStds;

    public LogisticRegressionModel() {
        super("Logistic Regression", TaskType.CLASSIFICATION);
    }

    private double sigmoid(double z) {
        if (z < -40.0) return 0.0;
        if (z > 40.0) return 1.0;
        return 1.0 / (1.0 + Math.exp(-z));
    }

    @Override
    public void fit(double[][] X, double[] y, Map<String, Object> params) {
        validateInput(X, y);
        int nSamples = X.length;
        int nFeatures = X[0].length;

        double learningRate = 0.05;
        int epochs = 600;
        if (params != null) {
            if (params.containsKey("learningRate")) {
                learningRate = ((Number) params.get("learningRate")).doubleValue();
            }
            if (params.containsKey("epochs")) {
                epochs = ((Number) params.get("epochs")).intValue();
            }
        }

        // Standard scaling
        featureMeans = new double[nFeatures];
        featureStds = new double[nFeatures];
        for (int j = 0; j < nFeatures; j++) {
            double sum = 0.0;
            for (int i = 0; i < nSamples; i++) sum += X[i][j];
            featureMeans[j] = sum / nSamples;

            double varSum = 0.0;
            for (int i = 0; i < nSamples; i++) varSum += Math.pow(X[i][j] - featureMeans[j], 2);
            featureStds[j] = Math.sqrt(varSum / nSamples);
            if (featureStds[j] == 0) featureStds[j] = 1.0;
        }

        double[][] normX = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                normX[i][j] = (X[i][j] - featureMeans[j]) / featureStds[j];
            }
        }

        weights = new double[nFeatures];
        bias = 0.0;

        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] dw = new double[nFeatures];
            double db = 0.0;

            for (int i = 0; i < nSamples; i++) {
                double linear = bias;
                for (int j = 0; j < nFeatures; j++) {
                    linear += weights[j] * normX[i][j];
                }
                double prob = sigmoid(linear);
                double error = prob - y[i];

                for (int j = 0; j < nFeatures; j++) {
                    dw[j] += error * normX[i][j];
                }
                db += error;
            }

            for (int j = 0; j < nFeatures; j++) {
                weights[j] -= learningRate * (dw[j] / nSamples);
            }
            bias -= learningRate * (db / nSamples);
        }

        this.isFitted = true;
    }

    @Override
    public double[] predict(double[][] X) {
        checkFitted();
        int nSamples = X.length;
        int nFeatures = X[0].length;
        double[] preds = new double[nSamples];

        for (int i = 0; i < nSamples; i++) {
            double linear = bias;
            for (int j = 0; j < nFeatures; j++) {
                double normVal = (X[i][j] - featureMeans[j]) / featureStds[j];
                linear += weights[j] * normVal;
            }
            double prob = sigmoid(linear);
            preds[i] = prob >= 0.5 ? 1.0 : 0.0;
        }
        return preds;
    }

    @Override
    public Map<String, Double> getFeatureImportance(List<String> featureNames) {
        checkFitted();
        Map<String, Double> importance = new LinkedHashMap<>();
        double total = 0.0;
        for (double w : weights) total += Math.abs(w);
        if (total == 0) total = 1.0;

        for (int i = 0; i < featureNames.size() && i < weights.length; i++) {
            double imp = Math.abs(weights[i]) / total;
            importance.put(featureNames.get(i), round(imp));
        }
        return importance;
    }
}
