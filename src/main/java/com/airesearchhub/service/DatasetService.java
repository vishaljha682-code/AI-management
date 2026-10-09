package com.airesearchhub.service;

import com.airesearchhub.dao.DatasetDao;
import com.airesearchhub.dao.DatasetDaoImpl;
import com.airesearchhub.exception.ResourceNotFoundException;
import com.airesearchhub.exception.ValidationException;
import com.airesearchhub.ml.DataFrame;
import com.airesearchhub.ml.DataSummary;
import com.airesearchhub.model.entity.Dataset;
import com.airesearchhub.model.entity.User;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatasetService {
    private final DatasetDao datasetDao;
    private final Gson gson;

    public DatasetService() {
        this.datasetDao = new DatasetDaoImpl();
        this.gson = new Gson();
        ensureSampleDatasetsSeeded();
    }

    public List<Dataset> getAllDatasets() {
        return datasetDao.findAll();
    }

    public List<Dataset> getUserDatasets(Long userId) {
        return datasetDao.findByUploadedById(userId);
    }

    public Dataset getDatasetById(Long id) {
        return datasetDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found with id: " + id));
    }

    public Dataset saveUploadedDataset(String name, String description, String originalFilename,
                                       InputStream inputStream, User user) throws IOException {
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".csv") && !originalFilename.toLowerCase().endsWith(".txt"))) {
            throw new ValidationException("Only CSV files are currently supported for automated analysis.");
        }

        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String safeFilename = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        File destFile = new File(uploadDir, safeFilename);

        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = inputStream.read(buf)) != -1) {
                fos.write(buf, 0, read);
            }
        }

        // Parse and validate with DataFrame
        DataFrame df = DataFrame.fromCsv(destFile);
        if (df.getRowCount() == 0 || df.getColumnCount() == 0) {
            destFile.delete();
            throw new ValidationException("The uploaded file appears to be empty or improperly formatted.");
        }

        Dataset d = new Dataset();
        d.setName(name != null && !name.trim().isEmpty() ? name.trim() : originalFilename);
        d.setDescription(description);
        d.setFilename(originalFilename);
        d.setFilePath(destFile.getAbsolutePath());
        d.setFileSize(destFile.length());
        d.setRowCount(df.getRowCount());
        d.setColumnCount(df.getColumnCount());
        d.setColumnsJson(gson.toJson(df.getColumnNames()));
        d.setUploadedById(user.getId());
        d.setUploaderName(user.getFullName() != null ? user.getFullName() : user.getUsername());

        return datasetDao.save(d);
    }

    public DataSummary analyzeDataset(Long datasetId) throws IOException {
        Dataset dataset = getDatasetById(datasetId);
        File file = new File(dataset.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("Underlying file not found on server disk.");
        }

        DataFrame df = DataFrame.fromCsv(file);
        DataSummary summary = new DataSummary();
        summary.setRowCount(df.getRowCount());
        summary.setColumnCount(df.getColumnCount());
        summary.setColumnNames(df.getColumnNames());
        summary.setColumnTypes(df.getColumnTypes());
        summary.setDuplicateRows(df.countDuplicates());

        Map<String, Integer> missing = df.countMissingValues();
        summary.setMissingValues(missing);

        Map<String, Double> missingPct = new LinkedHashMap<>();
        int rows = df.getRowCount();
        for (Map.Entry<String, Integer> e : missing.entrySet()) {
            double pct = rows > 0 ? (double) e.getValue() / rows * 100.0 : 0.0;
            missingPct.put(e.getKey(), Math.round(pct * 100.0) / 100.0);
        }
        summary.setMissingPercentages(missingPct);

        summary.setDescriptiveStats(df.computeDescriptiveStatistics());
        summary.setCorrelationMatrix(df.computeCorrelationMatrix());
        summary.setPreviewRows(df.getHead(15));

        return summary;
    }

    public boolean deleteDataset(Long id) {
        Dataset dataset = datasetDao.findById(id).orElse(null);
        if (dataset != null) {
            File f = new File(dataset.getFilePath());
            if (f.exists()) f.delete();
            return datasetDao.deleteById(id);
        }
        return false;
    }

    private void ensureSampleDatasetsSeeded() {
        try {
            if (datasetDao.count() == 0) {
                // Register sample housing dataset
                File f1 = new File("src/main/resources/sample_datasets/housing_regression.csv");
                if (f1.exists()) {
                    DataFrame df = DataFrame.fromCsv(f1);
                    Dataset d1 = new Dataset();
                    d1.setName("California Real Estate Valuations");
                    d1.setDescription("Housing market data containing area, rooms, transit distance, and sale valuation.");
                    d1.setFilename("housing_regression.csv");
                    d1.setFilePath(f1.getAbsolutePath());
                    d1.setFileSize(f1.length());
                    d1.setRowCount(df.getRowCount());
                    d1.setColumnCount(df.getColumnCount());
                    d1.setColumnsJson(gson.toJson(df.getColumnNames()));
                    d1.setUploadedById(1L);
                    d1.setUploaderName("Platform Administrator");
                    datasetDao.save(d1);
                }

                // Register sample churn dataset
                File f2 = new File("src/main/resources/sample_datasets/churn_classification.csv");
                if (f2.exists()) {
                    DataFrame df = DataFrame.fromCsv(f2);
                    Dataset d2 = new Dataset();
                    d2.setName("Telecom Customer Churn Analytics");
                    d2.setDescription("Subscriber metrics, billing charges, support calls, and customer retention flag.");
                    d2.setFilename("churn_classification.csv");
                    d2.setFilePath(f2.getAbsolutePath());
                    d2.setFileSize(f2.length());
                    d2.setRowCount(df.getRowCount());
                    d2.setColumnCount(df.getColumnCount());
                    d2.setColumnsJson(gson.toJson(df.getColumnNames()));
                    d2.setUploadedById(1L);
                    d2.setUploaderName("Platform Administrator");
                    datasetDao.save(d2);
                }

                // Register sample customer segmentation dataset
                File f3 = new File("src/main/resources/sample_datasets/customer_segmentation.csv");
                if (f3.exists()) {
                    DataFrame df = DataFrame.fromCsv(f3);
                    Dataset d3 = new Dataset();
                    d3.setName("Client Segmentation & Spending Behaviour");
                    d3.setDescription("Unsupervised client clustering data with annual income and spending score.");
                    d3.setFilename("customer_segmentation.csv");
                    d3.setFilePath(f3.getAbsolutePath());
                    d3.setFileSize(f3.length());
                    d3.setRowCount(df.getRowCount());
                    d3.setColumnCount(df.getColumnCount());
                    d3.setColumnsJson(gson.toJson(df.getColumnNames()));
                    d3.setUploadedById(1L);
                    d3.setUploaderName("Platform Administrator");
                    datasetDao.save(d3);
                }
            }
        } catch (Exception ignored) {}
    }
}
