package com.airesearchhub.dto;

import java.util.List;

public class ExperimentCompareDto {
    private List<Long> experimentIds;

    public ExperimentCompareDto() {}

    public List<Long> getExperimentIds() {
        return experimentIds;
    }

    public void setExperimentIds(List<Long> experimentIds) {
        this.experimentIds = experimentIds;
    }
}
