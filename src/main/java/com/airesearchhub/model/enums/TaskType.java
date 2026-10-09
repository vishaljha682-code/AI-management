package com.airesearchhub.model.enums;

public enum TaskType {
    REGRESSION("Regression"),
    CLASSIFICATION("Classification"),
    CLUSTERING("Clustering");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
