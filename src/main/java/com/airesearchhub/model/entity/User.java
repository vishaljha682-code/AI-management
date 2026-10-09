package com.airesearchhub.model.entity;

import com.airesearchhub.model.enums.Role;

public class User extends BaseEntity {
    private String username;
    private String fullName;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private double storageLimitMb;
    private double computeHoursQuota;

    public User() {
        super();
        this.role = Role.RESEARCHER;
        this.active = true;
        this.storageLimitMb = 1024.0; // 1 GB default
        this.computeHoursQuota = 50.0; // 50 hours
    }

    public User(String username, String fullName, String email, String passwordHash, Role role) {
        this();
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : Role.RESEARCHER;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getStorageLimitMb() {
        return storageLimitMb;
    }

    public void setStorageLimitMb(double storageLimitMb) {
        this.storageLimitMb = storageLimitMb;
    }

    public double getComputeHoursQuota() {
        return computeHoursQuota;
    }

    public void setComputeHoursQuota(double computeHoursQuota) {
        this.computeHoursQuota = computeHoursQuota;
    }
}
