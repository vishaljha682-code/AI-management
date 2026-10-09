package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.dto.LoginRequest;
import com.airesearchhub.dto.RegisterRequest;
import com.airesearchhub.exception.AuthenticationException;
import com.airesearchhub.exception.ValidationException;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.service.UserService;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthServlet extends BaseServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        if (path.equals("/me") || path.isEmpty() || path.equals("/")) {
            User user = getAuthenticatedUser(req);
            if (user != null) {
                // Return fresh copy
                User fresh = userService.findById(user.getId()).orElse(user);
                fresh.setPasswordHash(null); // Never leak password hash
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(fresh));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "No active session found.");
            }
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            if (path.equals("/login")) {
                String body = readRequestBody(req);
                LoginRequest loginReq = gson.fromJson(body, LoginRequest.class);
                User user = userService.authenticate(loginReq, getClientIp(req));
                setSessionUser(req, user);
                user.setPasswordHash(null);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Login successful", user));

            } else if (path.equals("/register")) {
                String body = readRequestBody(req);
                RegisterRequest regReq = gson.fromJson(body, RegisterRequest.class);
                User user = userService.register(regReq, getClientIp(req));
                setSessionUser(req, user);
                user.setPasswordHash(null);
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Account created successfully", user));

            } else if (path.equals("/logout")) {
                clearSessionUser(req);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Logged out successfully", null));

            } else if (path.equals("/change-password")) {
                User user = getAuthenticatedUser(req);
                if (user == null) {
                    sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
                String body = readRequestBody(req);
                @SuppressWarnings("unchecked")
                Map<String, String> map = gson.fromJson(body, Map.class);
                String oldPass = map.get("oldPassword");
                String newPass = map.get("newPassword");
                boolean success = userService.updatePassword(user.getId(), oldPass, newPass);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Password updated successfully", success));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
