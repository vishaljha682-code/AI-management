package com.airesearchhub.dao;

import com.airesearchhub.model.entity.Experiment;
import java.util.List;

public interface ExperimentDao extends GenericDao<Experiment, Long> {
    List<Experiment> findByProjectId(Long projectId);
    List<Experiment> findByDatasetId(Long datasetId);
    List<Experiment> findByIds(List<Long> ids);
}
