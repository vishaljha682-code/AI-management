package com.airesearchhub.ml.algorithms;

import com.airesearchhub.ml.BaseModel;
import com.airesearchhub.model.enums.TaskType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Random Forest Ensemble Model demonstrating composition and parallel bagging.
 */
public class RandomForestModel extends BaseModel {
    private List<DecisionTreeModel> trees;
    private int nEstimators = 10;
    private int maxDepth = 4;
    private final Random random = new Random(42);

    public RandomForestModel() {
        super("Random Forest", TaskType.CLASSIFICATION);
        this.trees = new ArrayList<>();
    }

    @Override
    public void fit(double[][] X, double[] y, Map<String, Object> params) {
        validateInput(X, y);
        if (params != null) {
            if (params.containsKey("nEstimators")) {
                nEstimators = ((Number) params.get("nEstimators")).intValue();
            }
            if (params.containsKey("maxDepth")) {
                maxDepth = ((Number) params.get("maxDepth")).intValue();
            }
        }

        trees.clear();
        int nSamples = X.length;

        // Train multiple bootstrap trees
        for (int t = 0; t < nEstimators; t++) {
            double[][] sampleX = new double[nSamples][X[0].length];
            double[] sampleY = new double[nSamples];

            // Bootstrap sample with replacement
            for (int i = 0; i < nSamples; i++) {
                int idx = random.nextInt(nSamples);
                sampleX[i] = X[idx];
                sampleY[i] = y[idx];
            }

            DecisionTreeModel tree = new DecisionTreeModel();
            Map<String, Object> treeParams = new HashMap<>();
            treeParams.put("maxDepth", maxDepth);
            treeParams.put("minSamplesSplit", 2);
            tree.fit(sampleX, sampleY, treeParams);
            trees.add(tree);
        }

        this.isFitted = true;
    }

    @Override
    public double[] predict(double[][] X) {
        checkFitted();
        int nSamples = X.length;
        double[] finalPreds = new double[nSamples];

        double[][] treeVotes = new double[trees.size()][nSamples];
        for (int t = 0; t < trees.size(); t++) {
            treeVotes[t] = trees.get(t).predict(X);
        }

        // Majority vote for each sample
        for (int i = 0; i < nSamples; i++) {
            Map<Double, Integer> voteCount = new HashMap<>();
            for (int t = 0; t < trees.size(); t++) {
                double vote = treeVotes[t][i];
                voteCount.put(vote, voteCount.getOrDefault(vote, 0) + 1);
            }

            double majority = treeVotes[0][i];
            int maxVotes = -1;
            for (Map.Entry<Double, Integer> e : voteCount.entrySet()) {
                if (e.getValue() > maxVotes) {
                    maxVotes = e.getValue();
                    majority = e.getKey();
                }
            }
            finalPreds[i] = majority;
        }

        return finalPreds;
    }

    @Override
    public Map<String, Double> getFeatureImportance(List<String> featureNames) {
        checkFitted();
        Map<String, Double> combined = new LinkedHashMap<>();
        for (String f : featureNames) combined.put(f, 0.0);

        for (DecisionTreeModel tree : trees) {
            Map<String, Double> treeImp = tree.getFeatureImportance(featureNames);
            for (String f : featureNames) {
                combined.put(f, combined.get(f) + treeImp.getOrDefault(f, 0.0));
            }
        }

        double total = 0.0;
        for (double v : combined.values()) total += v;
        if (total == 0) total = 1.0;

        for (String f : featureNames) {
            combined.put(f, round(combined.get(f) / total));
        }
        return combined;
    }
}
