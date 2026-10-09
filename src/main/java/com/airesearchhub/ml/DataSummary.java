package com.airesearchhub.ml;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DataSummary implements Serializable {
    private int rowCount;
    private int columnCount;
    private int duplicateRows;
    private List<String> columnNames;
    private Map<String, String> columnTypes;
    private Map<String, Integer> missingValues;
    private Map<String, Double> missingPercentages;
    private Map<String, Map<String, Double>> descriptiveStats;
    private Map<String, Map<String, Double>> correlationMatrix;
    private List<Map<String, Object>> previewRows;

    public DataSummary() {}

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getDuplicateRows() {
        return duplicateRows;
    }

    public void setDuplicateRows(int duplicateRows) {
        this.duplicateRows = duplicateRows;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public Map<String, String> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(Map<String, String> columnTypes) {
        this.columnTypes = columnTypes;
    }

    public Map<String, Integer> getMissingValues() {
        return missingValues;
    }

    public void setMissingValues(Map<String, Integer> missingValues) {
        this.missingValues = missingValues;
    }

    public Map<String, Double> getMissingPercentages() {
        return missingPercentages;
    }

    public void setMissingPercentages(Map<String, Double> missingPercentages) {
        this.missingPercentages = missingPercentages;
    }

    public Map<String, Map<String, Double>> getDescriptiveStats() {
        return descriptiveStats;
    }

    public void setDescriptiveStats(Map<String, Map<String, Double>> descriptiveStats) {
        this.descriptiveStats = descriptiveStats;
    }

    public Map<String, Map<String, Double>> getCorrelationMatrix() {
        return correlationMatrix;
    }

    public void setCorrelationMatrix(Map<String, Map<String, Double>> correlationMatrix) {
        this.correlationMatrix = correlationMatrix;
    }

    public List<Map<String, Object>> getPreviewRows() {
        return previewRows;
    }

    public void setPreviewRows(List<Map<String, Object>> previewRows) {
        this.previewRows = previewRows;
    }
}
