package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.dto.AssistantRequestDto;
import com.airesearchhub.model.entity.ChatMessage;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.service.AssistantService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AssistantServlet extends BaseServlet {
    private final AssistantService assistantService = new AssistantService();

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
            if (path.startsWith("/history/")) {
                String convId = path.substring("/history/".length());
                List<ChatMessage> history = assistantService.getHistory(convId);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(history));

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
            if (path.equals("/chat") || path.isEmpty() || path.equals("/")) {
                String body = readRequestBody(req);
                AssistantRequestDto dto = gson.fromJson(body, AssistantRequestDto.class);
                ChatMessage reply = assistantService.processQuery(dto, user);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(reply));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
