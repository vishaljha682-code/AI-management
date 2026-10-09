package com.airesearchhub.ml.algorithms;

import com.airesearchhub.ml.BaseModel;
import com.airesearchhub.model.enums.TaskType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * K-Nearest Neighbors (KNN) implementation for classification and regression.
 */
public class KNNModel extends BaseModel {
    private double[][] trainX;
    private double[] trainY;
    private int k = 3;

    public KNNModel() {
        super("K-Nearest Neighbors", TaskType.CLASSIFICATION);
    }

    private static class Neighbor {
        int index;
        double distance;
        Neighbor(int index, double distance) {
            this.index = index;
            this.distance = distance;
        }
    }

    @Override
    public void fit(double[][] X, double[] y, Map<String, Object> params) {
        validateInput(X, y);
        if (params != null && params.containsKey("k")) {
            this.k = ((Number) params.get("k")).intValue();
        }
        this.trainX = X;
        this.trainY = y;
        this.isFitted = true;
    }

    @Override
    public double[] predict(double[][] X) {
        checkFitted();
        double[] preds = new double[X.length];

        for (int i = 0; i < X.length; i++) {
            List<Neighbor> neighbors = new ArrayList<>();
            for (int j = 0; j < trainX.length; j++) {
                double dist = euclideanDistance(X[i], trainX[j]);
                neighbors.add(new Neighbor(j, dist));
            }

            Collections.sort(neighbors, Comparator.comparingDouble(n -> n.distance));

            // Majority vote among top k neighbors
            Map<Double, Integer> votes = new HashMap<>();
            int actualK = Math.min(k, neighbors.size());
            for (int m = 0; m < actualK; m++) {
                double label = trainY[neighbors.get(m).index];
                votes.put(label, votes.getOrDefault(label, 0) + 1);
            }

            double predictedClass = trainY[neighbors.get(0).index];
            int maxVotes = -1;
            for (Map.Entry<Double, Integer> e : votes.entrySet()) {
                if (e.getValue() > maxVotes) {
                    maxVotes = e.getValue();
                    predictedClass = e.getKey();
                }
            }
            preds[i] = predictedClass;
        }
        return preds;
    }

    private double euclideanDistance(double[] p1, double[] p2) {
        double sum = 0.0;
        for (int i = 0; i < p1.length && i < p2.length; i++) {
            sum += Math.pow(p1[i] - p2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
