package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.dto.ExperimentCompareDto;
import com.airesearchhub.model.entity.Experiment;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.service.ExperimentService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExperimentServlet extends BaseServlet {
    private final ExperimentService experimentService = new ExperimentService();

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
                String projectIdParam = req.getParameter("projectId");
                List<Experiment> list;
                if (projectIdParam != null && !projectIdParam.isEmpty()) {
                    list = experimentService.getExperimentsByProject(Long.parseLong(projectIdParam));
                } else {
                    list = experimentService.getAllExperiments();
                }
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(list));

            } else if (path.matches("^/\\d+$")) {
                Long id = Long.parseLong(path.substring(1));
                Experiment exp = experimentService.getExperimentById(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(exp));

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
            if (path.equals("/compare")) {
                String body = readRequestBody(req);
                ExperimentCompareDto dto = gson.fromJson(body, ExperimentCompareDto.class);
                Map<String, Object> comparison = experimentService.compareExperiments(dto.getExperimentIds());
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(comparison));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
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
                boolean success = experimentService.deleteExperiment(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Experiment deleted", success));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
