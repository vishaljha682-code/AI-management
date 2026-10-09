package com.airesearchhub.dao;

import com.airesearchhub.model.entity.Project;
import com.airesearchhub.model.entity.ProjectMember;
import java.util.List;

public interface ProjectDao extends GenericDao<Project, Long> {
    List<Project> findByUserId(Long userId);
    List<ProjectMember> findMembersByProjectId(Long projectId);
    boolean addMember(ProjectMember member);
    boolean removeMember(Long projectId, Long userId);
}
