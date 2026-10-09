package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.model.entity.Project;
import com.airesearchhub.model.entity.ProjectMember;
import com.airesearchhub.model.entity.ProjectTask;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.model.enums.Role;
import com.airesearchhub.service.ProjectService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProjectServlet extends BaseServlet {
    private final ProjectService projectService = new ProjectService();

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
                List<Project> list = (user.getRole() == Role.ADMIN) ?
                        projectService.getAllProjects() :
                        projectService.getProjectsForUser(user.getId());
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(list));

            } else if (path.matches("^/\\d+$")) {
                Long id = Long.parseLong(path.substring(1));
                Project p = projectService.getProjectById(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(p));

            } else if (path.matches("^/\\d+/members$")) {
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[1]);
                List<ProjectMember> members = projectService.getProjectMembers(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(members));

            } else if (path.matches("^/\\d+/tasks$")) {
                String[] parts = path.split("/");
                Long id = Long.parseLong(parts[1]);
                List<ProjectTask> tasks = projectService.getTasksByProjectId(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(tasks));

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
            if (path.equals("/create") || path.isEmpty() || path.equals("/")) {
                String body = readRequestBody(req);
                Project p = gson.fromJson(body, Project.class);
                Project created = projectService.createProject(p, user);
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Project created", created));

            } else if (path.matches("^/\\d+/members$")) {
                String[] parts = path.split("/");
                Long projectId = Long.parseLong(parts[1]);
                String body = readRequestBody(req);
                @SuppressWarnings("unchecked")
                Map<String, Object> map = gson.fromJson(body, Map.class);
                Long targetUserId = ((Number) map.get("userId")).longValue();
                String role = (String) map.get("projectRole");
                boolean success = projectService.addProjectMember(projectId, targetUserId, role);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Member added", success));

            } else if (path.matches("^/\\d+/tasks$")) {
                String[] parts = path.split("/");
                Long projectId = Long.parseLong(parts[1]);
                String body = readRequestBody(req);
                ProjectTask task = gson.fromJson(body, ProjectTask.class);
                task.setProjectId(projectId);
                ProjectTask created = projectService.createTask(task);
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, ApiResponse.success("Task created", created));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getAuthenticatedUser(req);
        if (user == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            if (path.equals("/update") || path.matches("^/\\d+$")) {
                String body = readRequestBody(req);
                Project p = gson.fromJson(body, Project.class);
                boolean success = projectService.updateProject(p);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Project updated", success));

            } else if (path.startsWith("/tasks/")) {
                String body = readRequestBody(req);
                ProjectTask task = gson.fromJson(body, ProjectTask.class);
                boolean success = projectService.updateTask(task);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Task updated", success));

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
                boolean success = projectService.deleteProject(id);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Project deleted", success));

            } else if (path.matches("^/\\d+/members/\\d+$")) {
                String[] parts = path.split("/");
                Long projectId = Long.parseLong(parts[1]);
                Long memberUserId = Long.parseLong(parts[3]);
                boolean success = projectService.removeProjectMember(projectId, memberUserId);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Member removed", success));

            } else if (path.startsWith("/tasks/")) {
                Long taskId = Long.parseLong(path.substring("/tasks/".length()));
                boolean success = projectService.deleteTask(taskId);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Task deleted", success));

            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
