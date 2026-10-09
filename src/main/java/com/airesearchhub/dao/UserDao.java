package com.airesearchhub.dao;

import com.airesearchhub.model.entity.User;
import com.airesearchhub.model.enums.Role;
import java.util.List;
import java.util.Optional;

public interface UserDao extends GenericDao<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String identifier);
    List<User> findByRole(Role role);
    boolean updatePassword(Long userId, String newPasswordHash);
    boolean updateStatus(Long userId, boolean active);
    boolean updateRole(Long userId, Role role);
}
