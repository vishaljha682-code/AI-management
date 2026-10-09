package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.dto.TrainingRequestDto;
import com.airesearchhub.model.entity.TrainingJob;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.service.TrainingService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TrainingJobServlet extends BaseServlet {
    private final TrainingService trainingService = new TrainingService();

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
                List<TrainingJob> list;
                if (projectIdParam != null && !projectIdParam.isEmpty()) {
                    list = trainingService.getJobsByProject(Long.parseLong(projectIdParam));
                } else {
                    list = trainingService.getAllJobs();
                }
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(list));

            } else if (path.matches("^/\\d+$")) {
                Long id = Long.parseLong(path.substring(1));
                TrainingJob job = trainingService.getJobById(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(job));

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
            if (path.equals("/start") || path.isEmpty() || path.equals("/")) {
                String body = readRequestBody(req);
                TrainingRequestDto dto = gson.fromJson(body, TrainingRequestDto.class);
                TrainingJob job = trainingService.startTrainingJob(dto, user);
                sendJsonResponse(resp, HttpServletResponse.SC_ACCEPTED, ApiResponse.success("Training job started in background thread", job));

            } else if (path.matches("^/\\d+/cancel$")) {
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[1]);
                boolean cancelled = trainingService.cancelJob(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Job cancellation requested", cancelled));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
