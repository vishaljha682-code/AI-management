package com.airesearchhub.service;

import com.airesearchhub.dao.ExperimentDao;
import com.airesearchhub.dao.ExperimentDaoImpl;
import com.airesearchhub.exception.ResourceNotFoundException;
import com.airesearchhub.model.entity.Experiment;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExperimentService {
    private final ExperimentDao experimentDao;
    private final Gson gson;

    public ExperimentService() {
        this.experimentDao = new ExperimentDaoImpl();
        this.gson = new Gson();
    }

    public List<Experiment> getAllExperiments() {
        return experimentDao.findAll();
    }

    public List<Experiment> getExperimentsByProject(Long projectId) {
        return experimentDao.findByProjectId(projectId);
    }

    public List<Experiment> getExperimentsByDataset(Long datasetId) {
        return experimentDao.findByDatasetId(datasetId);
    }

    public Experiment getExperimentById(Long id) {
        return experimentDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment not found: " + id));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> compareExperiments(List<Long> experimentIds) {
        List<Experiment> list = experimentDao.findByIds(experimentIds);
        Map<String, Object> comparison = new LinkedHashMap<>();

        List<Map<String, Object>> summaryList = new ArrayList<>();
        for (Experiment exp : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", exp.getId());
            item.put("name", exp.getExperimentName());
            item.put("algorithm", exp.getAlgorithm().getDisplayName());
            item.put("taskType", exp.getTaskType().name());
            item.put("datasetName", exp.getDatasetName());
            item.put("primaryMetricName", exp.getPrimaryMetricName());
            item.put("primaryMetricValue", exp.getPrimaryMetricValue());

            Map<String, Object> metrics = new LinkedHashMap<>();
            if (exp.getMetricsJson() != null) {
                try {
                    metrics = gson.fromJson(exp.getMetricsJson(), Map.class);
                } catch (Exception ignored) {}
            }
            item.put("metrics", metrics);

            Map<String, Object> params = new LinkedHashMap<>();
            if (exp.getHyperparametersJson() != null) {
                try {
                    params = gson.fromJson(exp.getHyperparametersJson(), Map.class);
                } catch (Exception ignored) {}
            }
            item.put("hyperparameters", params);

            summaryList.add(item);
        }

        comparison.put("experiments", summaryList);
        comparison.put("count", summaryList.size());
        return comparison;
    }

    public boolean deleteExperiment(Long id) {
        return experimentDao.deleteById(id);
    }
}
