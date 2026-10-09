package com.airesearchhub.ml;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class EvaluationMetrics implements Serializable {
    // Classification metrics
    private Double accuracy;
    private Double precision;
    private Double recall;
    private Double f1Score;
    private int[][] confusionMatrix;

    // Regression metrics
    private Double mae;
    private Double mse;
    private Double rmse;
    private Double r2Score;

    // Clustering metrics
    private Double silhouetteScore;
    private Double inertia;
    private Map<Integer, Integer> clusterSizes;

    public EvaluationMetrics() {
        this.clusterSizes = new LinkedHashMap<>();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (accuracy != null) map.put("accuracy", accuracy);
        if (precision != null) map.put("precision", precision);
        if (recall != null) map.put("recall", recall);
        if (f1Score != null) map.put("f1Score", f1Score);
        if (mae != null) map.put("mae", mae);
        if (mse != null) map.put("mse", mse);
        if (rmse != null) map.put("rmse", rmse);
        if (r2Score != null) map.put("r2Score", r2Score);
        if (silhouetteScore != null) map.put("silhouetteScore", silhouetteScore);
        if (inertia != null) map.put("inertia", inertia);
        return map;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getF1Score() {
        return f1Score;
    }

    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }

    public int[][] getConfusionMatrix() {
        return confusionMatrix;
    }

    public void setConfusionMatrix(int[][] confusionMatrix) {
        this.confusionMatrix = confusionMatrix;
    }

    public Double getMae() {
        return mae;
    }

    public void setMae(Double mae) {
        this.mae = mae;
    }

    public Double getMse() {
        return mse;
    }

    public void setMse(Double mse) {
        this.mse = mse;
    }

    public Double getRmse() {
        return rmse;
    }

    public void setRmse(Double rmse) {
        this.rmse = rmse;
    }

    public Double getR2Score() {
        return r2Score;
    }

    public void setR2Score(Double r2Score) {
        this.r2Score = r2Score;
    }

    public Double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(Double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    public Double getInertia() {
        return inertia;
    }

    public void setInertia(Double inertia) {
        this.inertia = inertia;
    }

    public Map<Integer, Integer> getClusterSizes() {
        return clusterSizes;
    }

    public void setClusterSizes(Map<Integer, Integer> clusterSizes) {
        this.clusterSizes = clusterSizes;
    }
}
