package com.airesearchhub.service;

import com.airesearchhub.dao.ProjectDao;
import com.airesearchhub.dao.ProjectDaoImpl;
import com.airesearchhub.dao.ProjectTaskDaoImpl;
import com.airesearchhub.dao.UserDao;
import com.airesearchhub.dao.UserDaoImpl;
import com.airesearchhub.exception.ResourceNotFoundException;
import com.airesearchhub.exception.ValidationException;
import com.airesearchhub.model.entity.Project;
import com.airesearchhub.model.entity.ProjectMember;
import com.airesearchhub.model.entity.ProjectTask;
import com.airesearchhub.model.entity.User;
import java.util.List;

public class ProjectService {
    private final ProjectDao projectDao;
    private final ProjectTaskDaoImpl taskDao;
    private final UserDao userDao;

    public ProjectService() {
        this.projectDao = new ProjectDaoImpl();
        this.taskDao = new ProjectTaskDaoImpl();
        this.userDao = new UserDaoImpl();
    }

    public List<Project> getAllProjects() {
        return projectDao.findAll();
    }

    public List<Project> getProjectsForUser(Long userId) {
        return projectDao.findByUserId(userId);
    }

    public Project getProjectById(Long id) {
        return projectDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public Project createProject(Project project, User user) {
        if (project.getTitle() == null || project.getTitle().trim().isEmpty()) {
            throw new ValidationException("Project title is required.");
        }
        project.setOwnerId(user.getId());
        project.setOwnerName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        return projectDao.save(project);
    }

    public boolean updateProject(Project project) {
        return projectDao.update(project);
    }

    public boolean deleteProject(Long id) {
        return projectDao.deleteById(id);
    }

    public List<ProjectMember> getProjectMembers(Long projectId) {
        return projectDao.findMembersByProjectId(projectId);
    }

    public boolean addProjectMember(Long projectId, Long userId, String roleInProject) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ValidationException("Target user not found."));
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(user.getId());
        member.setUserName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        member.setUserEmail(user.getEmail());
        member.setProjectRole(roleInProject != null ? roleInProject : "RESEARCHER");
        return projectDao.addMember(member);
    }

    public boolean removeProjectMember(Long projectId, Long userId) {
        return projectDao.removeMember(projectId, userId);
    }

    public List<ProjectTask> getTasksByProjectId(Long projectId) {
        return taskDao.findByProjectId(projectId);
    }

    public ProjectTask createTask(ProjectTask task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new ValidationException("Task title is required.");
        }
        return taskDao.save(task);
    }

    public boolean updateTask(ProjectTask task) {
        return taskDao.update(task);
    }

    public boolean deleteTask(Long taskId) {
        return taskDao.deleteById(taskId);
    }
}
