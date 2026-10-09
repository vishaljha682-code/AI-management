package com.airesearchhub.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * DataFrame provides in-memory tabular data storage, cleaning, statistics,
 * and train-test splitting in pure Core Java.
 * Demonstrates Collections (List, Map, Set) and Generics.
 */
public class DataFrame implements Serializable {
    private List<String> columnNames;
    private List<List<String>> rows;
    private Map<String, String> columnTypes; // "NUMERIC" or "CATEGORICAL"

    public DataFrame() {
        this.columnNames = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.columnTypes = new LinkedHashMap<>();
    }

    public DataFrame(List<String> columnNames, List<List<String>> rows) {
        this.columnNames = new ArrayList<>(columnNames);
        this.rows = new ArrayList<>(rows);
        this.columnTypes = new LinkedHashMap<>();
        inferColumnTypes();
    }

    public static DataFrame fromCsv(File file) throws IOException {
        List<String> headers = new ArrayList<>();
        List<List<String>> dataRows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                return new DataFrame();
            }

            // Parse header line
            String[] rawHeaders = parseCsvLine(line);
            for (String h : rawHeaders) {
                headers.add(h.trim().replaceAll("^\"|\"$", ""));
            }

            // Parse data lines
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] tokens = parseCsvLine(line);
                List<String> row = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) {
                    if (i < tokens.length) {
                        row.add(tokens[i].trim().replaceAll("^\"|\"$", ""));
                    } else {
                        row.add(""); // Missing value
                    }
                }
                dataRows.add(row);
            }
        }

        return new DataFrame(headers, dataRows);
    }

    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString());
        return result.toArray(new String[0]);
    }

    public void inferColumnTypes() {
        columnTypes.clear();
        for (int c = 0; c < columnNames.size(); c++) {
            String colName = columnNames.get(c);
            boolean isNumeric = true;
            int nonNullCount = 0;
            for (List<String> row : rows) {
                if (c < row.size()) {
                    String val = row.get(c);
                    if (val != null && !val.trim().isEmpty() && !val.equalsIgnoreCase("NA") && !val.equalsIgnoreCase("null")) {
                        nonNullCount++;
                        try {
                            Double.parseDouble(val.trim());
                        } catch (NumberFormatException e) {
                            isNumeric = false;
                            break;
                        }
                    }
                }
            }
            if (nonNullCount == 0 || !isNumeric) {
                columnTypes.put(colName, "CATEGORICAL");
            } else {
                columnTypes.put(colName, "NUMERIC");
            }
        }
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    public Map<String, String> getColumnTypes() {
        return Collections.unmodifiableMap(columnTypes);
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public List<Map<String, Object>> getHead(int limit) {
        List<Map<String, Object>> preview = new ArrayList<>();
        int count = Math.min(limit, rows.size());
        for (int i = 0; i < count; i++) {
            List<String> row = rows.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            for (int j = 0; j < columnNames.size(); j++) {
                String val = j < row.size() ? row.get(j) : "";
                map.put(columnNames.get(j), val);
            }
            preview.add(map);
        }
        return preview;
    }

    public int countDuplicates() {
        Set<String> set = new HashSet<>();
        int duplicates = 0;
        for (List<String> row : rows) {
            String key = String.join("||", row);
            if (!set.add(key)) {
                duplicates++;
            }
        }
        return duplicates;
    }

    public Map<String, Integer> countMissingValues() {
        Map<String, Integer> missing = new LinkedHashMap<>();
        for (int c = 0; c < columnNames.size(); c++) {
            String col = columnNames.get(c);
            int count = 0;
            for (List<String> row : rows) {
                if (c >= row.size() || row.get(c) == null || row.get(c).trim().isEmpty() ||
                        row.get(c).equalsIgnoreCase("NA") || row.get(c).equalsIgnoreCase("null")) {
                    count++;
                }
            }
            missing.put(col, count);
        }
        return missing;
    }

    public Map<String, Map<String, Double>> computeDescriptiveStatistics() {
        Map<String, Map<String, Double>> stats = new LinkedHashMap<>();
        for (int c = 0; c < columnNames.size(); c++) {
            String col = columnNames.get(c);
            if ("NUMERIC".equals(columnTypes.get(col))) {
                List<Double> values = new ArrayList<>();
                for (List<String> row : rows) {
                    if (c < row.size()) {
                        String s = row.get(c);
                        if (s != null && !s.trim().isEmpty()) {
                            try {
                                values.add(Double.parseDouble(s.trim()));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                if (!values.isEmpty()) {
                    Collections.sort(values);
                    double sum = 0.0;
                    for (double v : values) sum += v;
                    double mean = sum / values.size();

                    double varianceSum = 0.0;
                    for (double v : values) {
                        varianceSum += Math.pow(v - mean, 2);
                    }
                    double std = values.size() > 1 ? Math.sqrt(varianceSum / (values.size() - 1)) : 0.0;
                    double min = values.get(0);
                    double max = values.get(values.size() - 1);
                    double p25 = percentile(values, 25);
                    double median = percentile(values, 50);
                    double p75 = percentile(values, 75);

                    Map<String, Double> colStat = new LinkedHashMap<>();
                    colStat.put("count", (double) values.size());
                    colStat.put("mean", Math.round(mean * 1000.0) / 1000.0);
                    colStat.put("std", Math.round(std * 1000.0) / 1000.0);
                    colStat.put("min", min);
                    colStat.put("25%", p25);
                    colStat.put("50%", median);
                    colStat.put("75%", p75);
                    colStat.put("max", max);

                    stats.put(col, colStat);
                }
            }
        }
        return stats;
    }

    private double percentile(List<Double> sorted, double percentile) {
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    public Map<String, Map<String, Double>> computeCorrelationMatrix() {
        List<String> numericCols = new ArrayList<>();
        for (String c : columnNames) {
            if ("NUMERIC".equals(columnTypes.get(c))) {
                numericCols.add(c);
            }
        }

        Map<String, List<Double>> colData = new HashMap<>();
        for (String col : numericCols) {
            int idx = columnNames.indexOf(col);
            List<Double> list = new ArrayList<>();
            for (List<String> row : rows) {
                try {
                    list.add(Double.parseDouble(row.get(idx)));
                } catch (Exception e) {
                    list.add(0.0);
                }
            }
            colData.put(col, list);
        }

        Map<String, Map<String, Double>> matrix = new LinkedHashMap<>();
        for (String c1 : numericCols) {
            Map<String, Double> rowCorr = new LinkedHashMap<>();
            List<Double> v1 = colData.get(c1);
            for (String c2 : numericCols) {
                if (c1.equals(c2)) {
                    rowCorr.put(c2, 1.0);
                } else {
                    List<Double> v2 = colData.get(c2);
                    double corr = pearsonCorrelation(v1, v2);
                    rowCorr.put(c2, Math.round(corr * 1000.0) / 1000.0);
                }
            }
            matrix.put(c1, rowCorr);
        }
        return matrix;
    }

    private double pearsonCorrelation(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) return 0.0;
        int n = x.size();
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0;
        double sumX2 = 0.0, sumY2 = 0.0;

        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumX2 += xi * xi;
            sumY2 += yi * yi;
        }

        double numerator = (n * sumXY) - (sumX * sumY);
        double denomX = (n * sumX2) - (sumX * sumX);
        double denomY = (n * sumY2) - (sumY * sumY);
        double denominator = Math.sqrt(denomX * denomY);

        if (denominator == 0.0) return 0.0;
        return numerator / denominator;
    }

    public DataFrame[] trainTestSplit(double trainRatio) {
        List<List<String>> shuffled = new ArrayList<>(this.rows);
        Collections.shuffle(shuffled, new Random(42)); // Seed for reproducible splits

        int trainSize = (int) Math.round(shuffled.size() * trainRatio);
        List<List<String>> trainRows = shuffled.subList(0, trainSize);
        List<List<String>> testRows = shuffled.subList(trainSize, shuffled.size());

        DataFrame trainDf = new DataFrame(this.columnNames, trainRows);
        DataFrame testDf = new DataFrame(this.columnNames, testRows);
        return new DataFrame[]{trainDf, testDf};
    }

    public double[][] extractFeatures(List<String> featureCols) {
        double[][] X = new double[rows.size()][featureCols.size()];
        int[] indices = new int[featureCols.size()];
        for (int i = 0; i < featureCols.size(); i++) {
            String col = featureCols.get(i);
            int idx = columnNames.indexOf(col);
            if (idx == -1) {
                // Try case-insensitive matching
                for (int c = 0; c < columnNames.size(); c++) {
                    if (columnNames.get(c).equalsIgnoreCase(col)) {
                        idx = c;
                        break;
                    }
                }
            }
            if (idx == -1) {
                throw new IllegalArgumentException("Feature column '" + col + "' not found in columns: " + columnNames);
            }
            indices[i] = idx;
        }

        for (int r = 0; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            for (int c = 0; c < indices.length; c++) {
                int colIdx = indices[c];
                try {
                    X[r][c] = Double.parseDouble(row.get(colIdx));
                } catch (Exception e) {
                    X[r][c] = 0.0;
                }
            }
        }
        return X;
    }

    public double[] extractTarget(String targetCol) {
        int idx = columnNames.indexOf(targetCol);
        if (idx == -1) {
            for (int c = 0; c < columnNames.size(); c++) {
                if (columnNames.get(c).equalsIgnoreCase(targetCol)) {
                    idx = c;
                    break;
                }
            }
        }
        if (idx == -1) {
            throw new IllegalArgumentException("Target column '" + targetCol + "' not found in columns: " + columnNames);
        }
        double[] y = new double[rows.size()];
        for (int r = 0; r < rows.size(); r++) {
            try {
                y[r] = Double.parseDouble(rows.get(r).get(idx));
            } catch (Exception e) {
                y[r] = rows.get(r).get(idx).hashCode() % 1000;
            }
        }
        return y;
    }
}
