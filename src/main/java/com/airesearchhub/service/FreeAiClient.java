package com.airesearchhub.service;

import com.airesearchhub.config.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client for free and external AI APIs.
 * Supports:
 *  1. Pollinations.ai (100% Free, Zero API Key needed, multi-turn LLM)
 *  2. Google Gemini API (Free tier via AI Studio key)
 *  3. Groq / OpenAI compatible endpoints
 *  4. Graceful fallback to offline rule-based ML consultant if network is unavailable
 */
public class FreeAiClient {
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String DEFAULT_POLLINATIONS_URL = "https://text.pollinations.ai/";
    private static final String DEFAULT_SYSTEM_PROMPT = 
            "You are the expert AI Research Assistant & Machine Learning Copilot on AI ResearchHub, " +
            "a platform designed for B.Tech students, graduate researchers, and data scientists. " +
            "You specialize in machine learning (Linear/Logistic Regression, Decision Trees, Random Forest, KNN, K-Means), " +
            "deep learning, statistical evaluation metrics (Accuracy, Precision, Recall, F1, R2, RMSE), " +
            "Python/Java implementation code, and academic research guidance. " +
            "Format your answers with professional Markdown including headings, bullet points, and code blocks.";

    /**
     * Generate an AI response using the configured free or external LLM API.
     *
     * @param conversationHistory list of previous messages (maps with "role" and "content")
     * @param contextPrompt additional domain context about datasets/projects
     * @return AI generated text response
     * @throws Exception if external request fails (caller can fallback)
     */
    public static String generateResponse(List<Map<String, String>> conversationHistory, String contextPrompt) throws Exception {
        String apiKey = AppConfig.get("ai.api.key", "").trim();
        String provider = AppConfig.get("ai.provider", "free").trim().toLowerCase();

        // 1. If user supplied a Gemini API key or configured gemini
        if ("gemini".equalsIgnoreCase(provider) || apiKey.startsWith("AIza")) {
            return callGemini(conversationHistory, contextPrompt, apiKey);
        }

        // 2. If user supplied an OpenAI / Groq key
        if (("openai".equalsIgnoreCase(provider) || "groq".equalsIgnoreCase(provider)) && !apiKey.isEmpty()) {
            return callOpenAiCompatible(conversationHistory, contextPrompt, apiKey, provider);
        }

        // 3. Default: Zero-configuration 100% Free AI API (Pollinations.ai)
        return callPollinations(conversationHistory, contextPrompt);
    }

    /**
     * Calls Pollinations.ai (Free, no key required).
     */
    private static String callPollinations(List<Map<String, String>> conversationHistory, String contextPrompt) throws Exception {
        JsonObject requestJson = new JsonObject();
        JsonArray messages = new JsonArray();

        // System prompt with platform context
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        String fullSystem = DEFAULT_SYSTEM_PROMPT;
        if (contextPrompt != null && !contextPrompt.trim().isEmpty()) {
            fullSystem += "\n\n[Active Workspace Context]:\n" + contextPrompt;
        }
        sysMsg.addProperty("content", fullSystem);
        messages.add(sysMsg);

        // Append recent conversation history (up to last 10 turns)
        int startIdx = Math.max(0, conversationHistory.size() - 10);
        for (int i = startIdx; i < conversationHistory.size(); i++) {
            Map<String, String> m = conversationHistory.get(i);
            JsonObject turn = new JsonObject();
            String role = "user".equalsIgnoreCase(m.get("role")) ? "user" : "assistant";
            turn.addProperty("role", role);
            turn.addProperty("content", m.get("content"));
            messages.add(turn);
        }

        requestJson.add("messages", messages);
        requestJson.addProperty("model", AppConfig.get("ai.model", "openai"));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(DEFAULT_POLLINATIONS_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "text/plain, application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestJson)))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            String body = resp.body().trim();
            if (!body.isEmpty()) {
                return body;
            }
        }

        throw new RuntimeException("Pollinations API returned HTTP " + resp.statusCode() + ": " + resp.body());
    }

    /**
     * Calls Google Gemini API (free tier).
     */
    private static String callGemini(List<Map<String, String>> history, String contextPrompt, String apiKey) throws Exception {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();

        // System instruction
        JsonObject sysPart = new JsonObject();
        sysPart.addProperty("text", DEFAULT_SYSTEM_PROMPT + (contextPrompt != null ? "\nContext: " + contextPrompt : ""));
        JsonObject sysObj = new JsonObject();
        sysObj.add("parts", wrapSinglePart(sysPart));
        root.add("systemInstruction", sysObj);

        // Messages
        int startIdx = Math.max(0, history.size() - 10);
        for (int i = startIdx; i < history.size(); i++) {
            Map<String, String> m = history.get(i);
            JsonObject turn = new JsonObject();
            turn.addProperty("role", "user".equalsIgnoreCase(m.get("role")) ? "user" : "model");
            JsonObject part = new JsonObject();
            part.addProperty("text", m.get("content"));
            turn.add("parts", wrapSinglePart(part));
            contents.add(turn);
        }
        root.add("contents", contents);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(root)))
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            JsonObject resObj = gson.fromJson(resp.body(), JsonObject.class);
            JsonArray candidates = resObj.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                if (parts != null && parts.size() > 0) {
                    return parts.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
        }

        throw new RuntimeException("Gemini API error (HTTP " + resp.statusCode() + "): " + resp.body());
    }

    /**
     * Calls OpenAI or Groq chat completions endpoint.
     */
    private static String callOpenAiCompatible(List<Map<String, String>> history, String contextPrompt, String apiKey, String provider) throws Exception {
        String url = "groq".equalsIgnoreCase(provider)
                ? "https://api.groq.com/openai/v1/chat/completions"
                : "https://api.openai.com/v1/chat/completions";
        String defaultModel = "groq".equalsIgnoreCase(provider) ? "llama-3.1-8b-instant" : "gpt-4o-mini";
        String model = AppConfig.get("ai.model", defaultModel);

        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        JsonArray messages = new JsonArray();

        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", DEFAULT_SYSTEM_PROMPT + (contextPrompt != null ? "\nContext: " + contextPrompt : ""));
        messages.add(sysMsg);

        int startIdx = Math.max(0, history.size() - 10);
        for (int i = startIdx; i < history.size(); i++) {
            Map<String, String> m = history.get(i);
            JsonObject turn = new JsonObject();
            turn.addProperty("role", "user".equalsIgnoreCase(m.get("role")) ? "user" : "assistant");
            turn.addProperty("content", m.get("content"));
            messages.add(turn);
        }
        root.add("messages", messages);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(root)))
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            JsonObject resObj = gson.fromJson(resp.body(), JsonObject.class);
            JsonArray choices = resObj.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                return choices.get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
            }
        }

        throw new RuntimeException("OpenAI/Groq API error (HTTP " + resp.statusCode() + "): " + resp.body());
    }

    private static JsonArray wrapSinglePart(JsonObject part) {
        JsonArray arr = new JsonArray();
        arr.add(part);
        return arr;
    }
}
