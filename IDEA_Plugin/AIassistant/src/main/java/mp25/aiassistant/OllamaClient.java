package mp25.aiassistant;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class OllamaClient {
    private final String baseUrl;
    private final HttpClient client;
    private final Gson gson;
    private String currentModel;

    public OllamaClient(String baseUrl, String defaultModel) {
        this.baseUrl = baseUrl;
        this.currentModel = defaultModel;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public String generateCompletion(String prompt) throws IOException, InterruptedException {
        return generateCompletion(prompt, this.currentModel, false);
    }

    public String generateCompletion(String prompt, String model, boolean stream) throws IOException, InterruptedException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", stream);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API call failed with status code: " + response.statusCode());
        }

        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        return jsonResponse.get("response").getAsString();
    }

    public boolean isServerRunning() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public void setCurrentModel(String model) {
        this.currentModel = model;
    }

    public String getCurrentModel() {
        return currentModel;
    }
}