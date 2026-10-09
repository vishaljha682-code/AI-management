package com.airesearchhub.server;

import com.airesearchhub.config.AppConfig;
import com.airesearchhub.servlet.AdminServlet;
import com.airesearchhub.servlet.AssistantServlet;
import com.airesearchhub.servlet.AuthServlet;
import com.airesearchhub.servlet.DatasetServlet;
import com.airesearchhub.servlet.ExperimentServlet;
import com.airesearchhub.servlet.ProjectServlet;
import com.airesearchhub.servlet.TrainingJobServlet;
import com.airesearchhub.servlet.UsageServlet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class LightweightServer {
    private HttpServer server;
    private final int port;

    public LightweightServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor() != null ?
                Executors.newCachedThreadPool() : Executors.newFixedThreadPool(16));

        // Mount API Servlets via HttpServletAdapter
        server.createContext("/api/auth", new HttpServletAdapter(new AuthServlet(), "/api/auth"));
        server.createContext("/api/admin", new HttpServletAdapter(new AdminServlet(), "/api/admin"));
        server.createContext("/api/projects", new HttpServletAdapter(new ProjectServlet(), "/api/projects"));
        server.createContext("/api/datasets", new HttpServletAdapter(new DatasetServlet(), "/api/datasets"));
        server.createContext("/api/training-jobs", new HttpServletAdapter(new TrainingJobServlet(), "/api/training-jobs"));
        server.createContext("/api/experiments", new HttpServletAdapter(new ExperimentServlet(), "/api/experiments"));
        server.createContext("/api/assistant", new HttpServletAdapter(new AssistantServlet(), "/api/assistant"));
        server.createContext("/api/usage", new HttpServletAdapter(new UsageServlet(), "/api/usage"));

        // Static file handler for web UI
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("===============================================================");
        System.out.println("  AI ResearchHub Server running on http://localhost:" + port);
        System.out.println("  Embedded SQLite Database: " + AppConfig.get("db.sqlite.url"));
        System.out.println("  Default Accounts:");
        System.out.println("    Admin:      admin  / Admin@123");
        System.out.println("    Researcher: vishal / Vishal@123");
        System.out.println("===============================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
        }
    }

    static class StaticFileHandler implements HttpHandler {
        private final File staticDir = new File("src/main/resources/web");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File targetFile = new File(staticDir, path.replace('/', File.separatorChar));
            if (!targetFile.exists() || targetFile.isDirectory()) {
                targetFile = new File(staticDir, "index.html");
            }

            if (!targetFile.exists()) {
                String notFound = "<h1>404 Not Found</h1>";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
                return;
            }

            String mimeType = getMimeType(targetFile.getName());
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, targetFile.length());

            try (InputStream is = new FileInputStream(targetFile);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf)) != -1) {
                    os.write(buf, 0, read);
                }
            }
        }

        private String getMimeType(String name) {
            if (name.endsWith(".html")) return "text/html; charset=UTF-8";
            if (name.endsWith(".css")) return "text/css; charset=UTF-8";
            if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (name.endsWith(".json")) return "application/json; charset=UTF-8";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            if (name.endsWith(".svg")) return "image/svg+xml";
            if (name.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }
}
