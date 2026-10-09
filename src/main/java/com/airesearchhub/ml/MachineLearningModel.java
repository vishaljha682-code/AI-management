package com.airesearchhub.ml;

import com.airesearchhub.model.enums.TaskType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * MachineLearningModel Interface demonstrating Object-Oriented Polymorphism.
 * Implemented by concrete regression, classification, and clustering algorithms.
 */
public interface MachineLearningModel extends Serializable {
    void fit(double[][] X, double[] y, Map<String, Object> params);
    double[] predict(double[][] X);
    EvaluationMetrics evaluate(double[][] X_test, double[] y_test);
    String getAlgorithmName();
    TaskType getTaskType();
    Map<String, Double> getFeatureImportance(List<String> featureNames);
}
