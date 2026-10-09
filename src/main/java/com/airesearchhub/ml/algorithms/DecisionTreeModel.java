package com.airesearchhub.ml.algorithms;

import com.airesearchhub.ml.BaseModel;
import com.airesearchhub.model.enums.TaskType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CART Decision Tree implementation for classification and regression.
 */
public class DecisionTreeModel extends BaseModel {
    private Node root;
    private int maxDepth = 5;
    private int minSamplesSplit = 2;
    private Map<Integer, Double> featureImportanceMap;

    public DecisionTreeModel() {
        super("Decision Tree", TaskType.CLASSIFICATION);
        this.featureImportanceMap = new HashMap<>();
    }

    public static class Node implements Serializable {
        public int featureIndex = -1;
        public double threshold = 0.0;
        public Node left;
        public Node right;
        public Double value; // Leaf value

        public boolean isLeaf() {
            return value != null;
        }
    }

    @Override
    public void fit(double[][] X, double[] y, Map<String, Object> params) {
        validateInput(X, y);
        if (params != null) {
            if (params.containsKey("maxDepth")) {
                maxDepth = ((Number) params.get("maxDepth")).intValue();
            }
            if (params.containsKey("minSamplesSplit")) {
                minSamplesSplit = ((Number) params.get("minSamplesSplit")).intValue();
            }
        }
        featureImportanceMap.clear();
        root = buildTree(X, y, 0);
        this.isFitted = true;
    }

    private Node buildTree(double[][] X, double[] y, int depth) {
        int nSamples = X.length;
        int nFeatures = X[0].length;

        // Check stopping conditions
        boolean sameClass = true;
        for (int i = 1; i < y.length; i++) {
            if (y[i] != y[0]) {
                sameClass = false;
                break;
            }
        }

        if (depth >= maxDepth || nSamples < minSamplesSplit || sameClass) {
            Node leaf = new Node();
            leaf.value = calculateMajorityClass(y);
            return leaf;
        }

        // Find best split
        int bestFeature = -1;
        double bestThreshold = 0.0;
        double bestGiniGain = -1.0;

        double currentGini = calculateGini(y);

        for (int f = 0; f < nFeatures; f++) {
            for (int i = 0; i < nSamples; i++) {
                double threshold = X[i][f];
                List<Integer> leftIdx = new ArrayList<>();
                List<Integer> rightIdx = new ArrayList<>();

                for (int r = 0; r < nSamples; r++) {
                    if (X[r][f] <= threshold) leftIdx.add(r);
                    else rightIdx.add(r);
                }

                if (leftIdx.isEmpty() || rightIdx.isEmpty()) continue;

                double[] leftY = new double[leftIdx.size()];
                for (int j = 0; j < leftIdx.size(); j++) leftY[j] = y[leftIdx.get(j)];

                double[] rightY = new double[rightIdx.size()];
                for (int j = 0; j < rightIdx.size(); j++) rightY[j] = y[rightIdx.get(j)];

                double weightedGini = ((double) leftY.length / nSamples) * calculateGini(leftY) +
                        ((double) rightY.length / nSamples) * calculateGini(rightY);
                double gain = currentGini - weightedGini;

                if (gain > bestGiniGain) {
                    bestGiniGain = gain;
                    bestFeature = f;
                    bestThreshold = threshold;
                }
            }
        }

        if (bestFeature == -1 || bestGiniGain <= 0.0) {
            Node leaf = new Node();
            leaf.value = calculateMajorityClass(y);
            return leaf;
        }

        // Split data
        List<double[]> leftXList = new ArrayList<>();
        List<Double> leftYList = new ArrayList<>();
        List<double[]> rightXList = new ArrayList<>();
        List<Double> rightYList = new ArrayList<>();

        for (int i = 0; i < nSamples; i++) {
            if (X[i][bestFeature] <= bestThreshold) {
                leftXList.add(X[i]);
                leftYList.add(y[i]);
            } else {
                rightXList.add(X[i]);
                rightYList.add(y[i]);
            }
        }

        featureImportanceMap.put(bestFeature, featureImportanceMap.getOrDefault(bestFeature, 0.0) + bestGiniGain);

        Node node = new Node();
        node.featureIndex = bestFeature;
        node.threshold = bestThreshold;
        node.left = buildTree(leftXList.toArray(new double[0][]), toDoubleArray(leftYList), depth + 1);
        node.right = buildTree(rightXList.toArray(new double[0][]), toDoubleArray(rightYList), depth + 1);
        return node;
    }

    private double calculateGini(double[] y) {
        if (y.length == 0) return 0.0;
        Map<Double, Integer> counts = new HashMap<>();
        for (double v : y) counts.put(v, counts.getOrDefault(v, 0) + 1);
        double impurity = 1.0;
        for (int count : counts.values()) {
            double prob = (double) count / y.length;
            impurity -= prob * prob;
        }
        return impurity;
    }

    private double calculateMajorityClass(double[] y) {
        if (y.length == 0) return 0.0;
        Map<Double, Integer> counts = new HashMap<>();
        for (double v : y) counts.put(v, counts.getOrDefault(v, 0) + 1);
        double majority = y[0];
        int maxCount = -1;
        for (Map.Entry<Double, Integer> e : counts.entrySet()) {
            if (e.getValue() > maxCount) {
                maxCount = e.getValue();
                majority = e.getKey();
            }
        }
        return majority;
    }

    private double[] toDoubleArray(List<Double> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    @Override
    public double[] predict(double[][] X) {
        checkFitted();
        double[] preds = new double[X.length];
        for (int i = 0; i < X.length; i++) {
            preds[i] = predictRow(root, X[i]);
        }
        return preds;
    }

    private double predictRow(Node node, double[] x) {
        if (node.isLeaf()) {
            return node.value;
        }
        if (x[node.featureIndex] <= node.threshold) {
            return predictRow(node.left, x);
        } else {
            return predictRow(node.right, x);
        }
    }

    @Override
    public Map<String, Double> getFeatureImportance(List<String> featureNames) {
        Map<String, Double> result = new LinkedHashMap<>();
        double sum = 0.0;
        for (double val : featureImportanceMap.values()) sum += val;
        if (sum == 0.0) sum = 1.0;

        for (int i = 0; i < featureNames.size(); i++) {
            double imp = featureImportanceMap.getOrDefault(i, 0.0) / sum;
            result.put(featureNames.get(i), round(imp));
        }
        return result;
    }
}
