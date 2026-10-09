package com.airesearchhub.servlet;

import com.airesearchhub.dto.ApiResponse;
import com.airesearchhub.model.entity.User;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UsageServlet extends BaseServlet {

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
            if (path.equals("/reports") || path.equals("/metrics") || path.isEmpty() || path.equals("/")) {
                Map<String, Object> metrics = new LinkedHashMap<>();

                OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();

                metrics.put("availableProcessors", osBean.getAvailableProcessors());
                metrics.put("systemLoadAverage", osBean.getSystemLoadAverage());
                metrics.put("heapMemoryUsedMb", Math.round(memBean.getHeapMemoryUsage().getUsed() / (1024.0 * 1024.0) * 100.0) / 100.0);
                metrics.put("heapMemoryMaxMb", Math.round(memBean.getHeapMemoryUsage().getMax() / (1024.0 * 1024.0) * 100.0) / 100.0);
                metrics.put("nonHeapMemoryUsedMb", Math.round(memBean.getNonHeapMemoryUsage().getUsed() / (1024.0 * 1024.0) * 100.0) / 100.0);

                sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success(metrics));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
