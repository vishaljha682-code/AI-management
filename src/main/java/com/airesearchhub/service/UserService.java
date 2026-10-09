package com.airesearchhub.service;

import com.airesearchhub.config.DatabaseManager;
import com.airesearchhub.dao.ActivityLogDaoImpl;
import com.airesearchhub.dao.UserDao;
import com.airesearchhub.dao.UserDaoImpl;
import com.airesearchhub.dto.LoginRequest;
import com.airesearchhub.dto.RegisterRequest;
import com.airesearchhub.exception.AuthenticationException;
import com.airesearchhub.exception.ValidationException;
import com.airesearchhub.model.entity.ActivityLog;
import com.airesearchhub.model.entity.User;
import com.airesearchhub.model.enums.Role;
import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserDao userDao;
    private final ActivityLogDaoImpl activityLogDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
        this.activityLogDao = new ActivityLogDaoImpl();
    }

    public User authenticate(LoginRequest req, String ip) {
        if (req == null || req.getUsernameOrEmail() == null || req.getPassword() == null) {
            throw new ValidationException("Username/email and password are required.");
        }

        User user = userDao.findByUsernameOrEmail(req.getUsernameOrEmail().trim())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

        if (!user.isActive()) {
            throw new AuthenticationException("Your account has been deactivated. Please contact an administrator.");
        }

        String inputHash = DatabaseManager.hashPassword(req.getPassword().trim());
        if (!inputHash.equals(user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        // Log login
        activityLogDao.save(new ActivityLog(user.getId(), user.getUsername(), "USER_LOGIN", "User logged in successfully", ip));
        return user;
    }

    public User register(RegisterRequest req, String ip) {
        if (req == null || req.getUsername() == null || req.getEmail() == null || req.getPassword() == null) {
            throw new ValidationException("All registration fields are required.");
        }

        String username = req.getUsername().trim();
        String email = req.getEmail().trim();
        String password = req.getPassword().trim();

        if (username.length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long.");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new ValidationException("Please provide a valid email address.");
        }
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }

        if (userDao.findByUsername(username).isPresent()) {
            throw new ValidationException("Username is already taken.");
        }
        if (userDao.findByEmail(email).isPresent()) {
            throw new ValidationException("Email is already registered.");
        }

        Role role = Role.fromString(req.getRole());
        String passwordHash = DatabaseManager.hashPassword(password);

        User user = new User(username, req.getFullName() != null ? req.getFullName().trim() : username, email, passwordHash, role);
        User saved = userDao.save(user);

        activityLogDao.save(new ActivityLog(saved.getId(), saved.getUsername(), "USER_REGISTER", "New account registered", ip));
        return saved;
    }

    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
        String oldHash = DatabaseManager.hashPassword(oldPassword);
        if (!oldHash.equals(user.getPasswordHash())) {
            throw new ValidationException("Current password does not match.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters.");
        }
        String newHash = DatabaseManager.hashPassword(newPassword);
        return userDao.updatePassword(userId, newHash);
    }
}
