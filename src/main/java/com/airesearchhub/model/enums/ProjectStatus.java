package com.airesearchhub.model.enums;

public enum ProjectStatus {
    PLANNING("Planning"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    ARCHIVED("Archived");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ProjectStatus fromString(String text) {
        if (text == null) return ACTIVE;
        for (ProjectStatus s : ProjectStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.label.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return ACTIVE;
    }
}
