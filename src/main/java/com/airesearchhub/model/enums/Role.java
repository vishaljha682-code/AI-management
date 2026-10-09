package com.airesearchhub.model.enums;

public enum Role {
    ADMIN("Administrator"),
    RESEARCHER("Researcher");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        if (text == null) return RESEARCHER;
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(text) || r.displayName.equalsIgnoreCase(text)) {
                return r;
            }
        }
        return RESEARCHER;
    }
}
