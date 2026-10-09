package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.exception.AuthorizationException;
import com.airesearchhub.model.entity.ResourceAllocation;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.service.AdminService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminServlet extends BaseServlet {
    private final AdminService adminService = new AdminService();

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
            if (path.equals("/dashboard") || path.equals("/stats")) {
                Map<String, Object> stats = adminService.getDashboardStats(user);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(stats));

            } else if (path.equals("/users")) {
                List<User> users = adminService.listUsers(user);
                for (User u : users) u.setPasswordHash(null);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(users));

            } else if (path.equals("/resources")) {
                List<ResourceAllocation> allocations = adminService.listResourceAllocations(user);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(allocations));

            } else if (path.equals("/audit-logs")) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(adminService.getAuditLogs(user)));

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
            if (path.equals("/users/status")) {
                String body = readRequestBody(req);
                @SuppressWarnings("unchecked")
                Map<String, Object> map = gson.fromJson(body, Map.class);
                Long targetUserId = ((Number) map.get("userId")).longValue();
                boolean active = (Boolean) map.get("active");
                boolean success = adminService.updateUserStatus(targetUserId, active, user);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("User status updated", success));

            } else if (path.equals("/users/role")) {
                String body = readRequestBody(req);
                @SuppressWarnings("unchecked")
                Map<String, Object> map = gson.fromJson(body, Map.class);
                Long targetUserId = ((Number) map.get("userId")).longValue();
                String newRole = (String) map.get("role");
                boolean success = adminService.updateUserRole(targetUserId, newRole, user);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("User role updated", success));

            } else if (path.equals("/resources")) {
                String body = readRequestBody(req);
                ResourceAllocation alloc = gson.fromJson(body, ResourceAllocation.class);
                boolean success = adminService.updateResourceAllocation(alloc, user);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Resource allocation saved", success));

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
            if (path.startsWith("/users/")) {
                String idStr = path.substring("/users/".length());
                Long targetUserId = Long.parseLong(idStr);
                boolean success = adminService.deleteUser(targetUserId, user);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("User account deleted", success));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
