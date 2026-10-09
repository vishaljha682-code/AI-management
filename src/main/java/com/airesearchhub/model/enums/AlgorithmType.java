package com.airesearchhub.model.enums;

public enum AlgorithmType {
    LINEAR_REGRESSION("Linear Regression", TaskType.REGRESSION),
    LOGISTIC_REGRESSION("Logistic Regression", TaskType.CLASSIFICATION),
    DECISION_TREE("Decision Tree", TaskType.CLASSIFICATION),
    RANDOM_FOREST("Random Forest", TaskType.CLASSIFICATION),
    KNN("K-Nearest Neighbors", TaskType.CLASSIFICATION),
    KMEANS("K-Means Clustering", TaskType.CLUSTERING);

    private final String displayName;
    private final TaskType defaultTaskType;

    AlgorithmType(String displayName, TaskType defaultTaskType) {
        this.displayName = displayName;
        this.defaultTaskType = defaultTaskType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TaskType getDefaultTaskType() {
        return defaultTaskType;
    }

    public static AlgorithmType fromString(String text) {
        if (text == null) return LINEAR_REGRESSION;
        for (AlgorithmType a : AlgorithmType.values()) {
            if (a.name().equalsIgnoreCase(text) || a.displayName.equalsIgnoreCase(text)) {
                return a;
            }
        }
        return LINEAR_REGRESSION;
    }
}
