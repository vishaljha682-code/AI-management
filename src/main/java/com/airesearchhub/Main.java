package com.airesearchhub;

import com.airesearchhub.config.AppConfig;
import com.airesearchhub.config.DatabaseManager;
import com.airesearchhub.ml.engine.ModelTrainingEngine;
import com.airesearchhub.server.LightweightServer;

/**
 * Main application entrypoint for AI ResearchHub.
 */
public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Initializing AI ResearchHub platform...");

            // 1. Initialize Database & Seed data
            DatabaseManager.getInstance();
            System.out.println("Database and schema initialized successfully.");

            // 2. Start Lightweight Web Server on configured port
            int port = AppConfig.getInt("server.port", 8080);
            LightweightServer server = new LightweightServer(port);
            server.start();

            // 3. Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down AI ResearchHub...");
                ModelTrainingEngine.getInstance().shutdown();
                server.stop();
            }));

        } catch (Exception e) {
            System.err.println("Fatal error starting AI ResearchHub: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
