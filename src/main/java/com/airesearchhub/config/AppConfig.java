package com.airesearchhub.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        // Default properties
        properties.setProperty("server.port", "8080");
        properties.setProperty("db.type", "sqlite"); // sqlite or mysql
        properties.setProperty("db.sqlite.url", "jdbc:sqlite:data/ai_researchhub.db");
        properties.setProperty("db.mysql.url", "jdbc:mysql://localhost:3306/ai_researchhub?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        properties.setProperty("db.mysql.user", "root");
        properties.setProperty("db.mysql.password", "");
        properties.setProperty("jwt.secret", "ai_researchhub_super_secret_jwt_key_2026_xyz");
        properties.setProperty("upload.dir", "uploads");
        properties.setProperty("ai.api.key", ""); // Optional external LLM API key
        properties.setProperty("ai.model", "gpt-4o-mini");

        // Load from .env or application.properties if exists
        File propFile = new File("application.properties");
        if (propFile.exists()) {
            try (InputStream is = new FileInputStream(propFile)) {
                properties.load(is);
            } catch (Exception ignored) {}
        }
        
        // Override with system environment variables if present
        for (String key : properties.stringPropertyNames()) {
            String envKey = key.replace('.', '_').toUpperCase();
            String envVal = System.getenv(envKey);
            if (envVal != null && !envVal.trim().isEmpty()) {
                properties.setProperty(key, envVal);
            }
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean isMySql() {
        return "mysql".equalsIgnoreCase(get("db.type", "sqlite"));
    }
}
