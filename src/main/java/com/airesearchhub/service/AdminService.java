package com.airesearchhub.service;

import com.airesearchhub.dao.ActivityLogDaoImpl;
import com.airesearchhub.dao.DatasetDao;
import com.airesearchhub.dao.DatasetDaoImpl;
import com.airesearchhub.dao.ExperimentDao;
import com.airesearchhub.dao.ExperimentDaoImpl;
import com.airesearchhub.dao.ProjectDao;
import com.airesearchhub.dao.ProjectDaoImpl;
import com.airesearchhub.dao.ResourceDaoImpl;
import com.airesearchhub.dao.TrainingJobDao;
import com.airesearchhub.dao.TrainingJobDaoImpl;
import com.airesearchhub.dao.UserDao;
import com.airesearchhub.dao.UserDaoImpl;
import com.airesearchhub.exception.AuthorizationException;
import com.airesearchhub.exception.ResourceNotFoundException;
import com.airesearchhub.model.entity.ActivityLog;
import com.airesearchhub.model.entity.ResourceAllocation;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.model.enums.Role;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminService {
    private final UserDao userDao;
    private final ProjectDao projectDao;
    private final DatasetDao datasetDao;
    private final TrainingJobDao jobDao;
    private final ExperimentDao experimentDao;
    private final ResourceDaoImpl resourceDao;
    private final ActivityLogDaoImpl logDao;

    public AdminService() {
        this.userDao = new UserDaoImpl();
        this.projectDao = new ProjectDaoImpl();
        this.datasetDao = new DatasetDaoImpl();
        this.jobDao = new TrainingJobDaoImpl();
        this.experimentDao = new ExperimentDaoImpl();
        this.resourceDao = new ResourceDaoImpl();
        this.logDao = new ActivityLogDaoImpl();
    }

    public Map<String, Object> getDashboardStats(User requester) {
        verifyAdmin(requester);
        Map<String, Object> stats = new LinkedHashMap<>();

        List<User> users = userDao.findAll();
        int activeResearchers = 0;
        double totalStorageMb = 0.0;

        for (User u : users) {
            if (u.isActive() && u.getRole() == Role.RESEARCHER) {
                activeResearchers++;
            }
        }

        List<com.airesearchhub.model.entity.Dataset> datasets = datasetDao.findAll();
        for (com.airesearchhub.model.entity.Dataset d : datasets) {
            totalStorageMb += (double) d.getFileSize() / (1024 * 1024);
        }

        stats.put("totalUsers", users.size());
        stats.put("activeResearchers", activeResearchers);
        stats.put("totalProjects", projectDao.count());
        stats.put("totalDatasets", datasets.size());
        stats.put("totalTrainingJobs", jobDao.count());
        stats.put("totalExperiments", experimentDao.count());
        stats.put("storageUsageMb", Math.round(totalStorageMb * 100.0) / 100.0);

        return stats;
    }

    public List<User> listUsers(User requester) {
        verifyAdmin(requester);
        return userDao.findAll();
    }

    public boolean updateUserStatus(Long userId, boolean active, User requester) {
        verifyAdmin(requester);
        if (requester.getId().equals(userId)) {
            throw new AuthorizationException("Administrators cannot deactivate their own account.");
        }
        return userDao.updateStatus(userId, active);
    }

    public boolean updateUserRole(Long userId, String roleStr, User requester) {
        verifyAdmin(requester);
        Role role = Role.fromString(roleStr);
        return userDao.updateRole(userId, role);
    }

    public boolean deleteUser(Long userId, User requester) {
        verifyAdmin(requester);
        if (requester.getId().equals(userId)) {
            throw new AuthorizationException("Administrators cannot delete their own account.");
        }
        return userDao.deleteById(userId);
    }

    public List<ResourceAllocation> listResourceAllocations(User requester) {
        verifyAdmin(requester);
        return resourceDao.findAll();
    }

    public boolean updateResourceAllocation(ResourceAllocation allocation, User requester) {
        verifyAdmin(requester);
        return resourceDao.update(allocation);
    }

    public List<ActivityLog> getAuditLogs(User requester) {
        verifyAdmin(requester);
        return logDao.findRecent(100);
    }

    private void verifyAdmin(User user) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Administrative privileges required for this operation.");
        }
    }
}
