package com.airesearchhub.ml.algorithms;

import com.airesearchhub.ml.BaseModel;
import com.airesearchhub.model.enums.TaskType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Multivariate Linear Regression Model with gradient descent optimization.
 */
public class LinearRegressionModel extends BaseModel {
    private double[] weights;
    private double bias;
    private double[] featureMeans;
    private double[] featureStds;

    public LinearRegressionModel() {
        super("Linear Regression", TaskType.REGRESSION);
    }

    @Override
    public void fit(double[][] X, double[] y, Map<String, Object> params) {
        validateInput(X, y);
        int nSamples = X.length;
        int nFeatures = X[0].length;

        // Hyperparameters
        double learningRate = 0.01;
        int epochs = 500;
        if (params != null) {
            if (params.containsKey("learningRate")) {
                learningRate = ((Number) params.get("learningRate")).doubleValue();
            }
            if (params.containsKey("epochs")) {
                epochs = ((Number) params.get("epochs")).intValue();
            }
        }

        // Feature normalization (standard scaling)
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

        // Gradient Descent
        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] dw = new double[nFeatures];
            double db = 0.0;

            for (int i = 0; i < nSamples; i++) {
                double pred = bias;
                for (int j = 0; j < nFeatures; j++) {
                    pred += weights[j] * normX[i][j];
                }
                double error = pred - y[i];

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
            double pred = bias;
            for (int j = 0; j < nFeatures; j++) {
                double normVal = (X[i][j] - featureMeans[j]) / featureStds[j];
                pred += weights[j] * normVal;
            }
            preds[i] = pred;
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
