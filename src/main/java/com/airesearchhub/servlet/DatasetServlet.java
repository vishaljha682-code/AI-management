package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.exception.ValidationException;
import com.airesearchhub.ml.DataSummary;
import com.airesearchhub.model.entity.Dataset;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.service.DatasetService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DatasetServlet extends BaseServlet {
    private final DatasetService datasetService = new DatasetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            if (path.equals("/list") || path.isEmpty() || path.equals("/")) {
                List<Dataset> list = datasetService.getAllDatasets();
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(list));

            } else if (path.matches("^/\\d+$")) {
                Long id = Long.parseLong(path.substring(1));
                Dataset d = datasetService.getDatasetById(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(d));

            } else if (path.matches("^/\\d+/analysis$")) {
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[1]);
                DataSummary summary = datasetService.analyzeDataset(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(summary));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            if (path.equals("/upload") || path.isEmpty() || path.equals("/")) {
                String body = readRequestBody(req);
                @SuppressWarnings("unchecked")
                Map<String, String> map = gson.fromJson(body, Map.class);

                String name = map.get("name");
                String description = map.get("description");
                String filename = map.get("filename");
                String fileContent = map.get("content"); // Plain CSV text or Base64

                if (filename == null || filename.trim().isEmpty()) {
                    filename = "dataset_" + System.currentTimeMillis() + ".csv";
                }
                if (fileContent == null || fileContent.trim().isEmpty()) {
                    throw new ValidationException("CSV content is required.");
                }

                byte[] bytes;
                if (fileContent.startsWith("data:") || isBase64(fileContent)) {
                    String clean = fileContent.contains(",") ? fileContent.substring(fileContent.indexOf(",") + 1) : fileContent;
                    try {
                        bytes = Base64.getDecoder().decode(clean);
                    } catch (IllegalArgumentException e) {
                        bytes = fileContent.getBytes(StandardCharsets.UTF_8);
                    }
                } else {
                    bytes = fileContent.getBytes(StandardCharsets.UTF_8);
                }

                InputStream is = new ByteArrayInputStream(bytes);
                Dataset saved = datasetService.saveUploadedDataset(name, description, filename, is, user);
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Dataset uploaded successfully", saved));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private boolean isBase64(String s) {
        if (s == null || s.length() < 4) return false;
        return s.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            if (path.matches("^/\\d+$")) {
                Long id = Long.parseLong(path.substring(1));
                boolean success = datasetService.deleteDataset(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Dataset deleted", success));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
