package mp25.aiassistant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Service for communicating with Ollama API
 */
public class OllamaService {
    private static final String BASE_URL = "http://localhost:11434/";

    /**
     * Send prompt to the Ollama API asynchronously
     *
     * @param model The model name to use
     * @param prompt The user's prompt
     * @return CompletableFuture with the response string
     */
    public static CompletableFuture<String> generateResponse(String model, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(BASE_URL+"api/generate");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create request JSON
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", model);
                requestBody.put("prompt", prompt);
                requestBody.put("stream", false);  // Get complete response at once

                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Read response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine).append("\r");
                        }

                        // Parse JSON response
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        return jsonResponse.getString("response");
                    }
                } else {
                    return "Error: HTTP " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error connecting to Ollama: " + e.getMessage();
            }
        });
    }

    /**
     * Get the list of available models from the Ollama API
     *
     * @return CompletableFuture with the JSON response string
     */public static CompletableFuture<String[]> getModels() {
         return CompletableFuture.supplyAsync(() -> {
             try {
                 URL url = new URL(BASE_URL + "api/tags");
                 HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                 connection.setRequestMethod("GET");
                 connection.setRequestProperty("Content-Type", "application/json");

                 // Read response
                 int responseCode = connection.getResponseCode();
                 if (responseCode == HttpURLConnection.HTTP_OK) {
                     try (BufferedReader br = new BufferedReader(
                             new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                         StringBuilder response = new StringBuilder();
                         String responseLine;
                         while ((responseLine = br.readLine()) != null) {
                             response.append(responseLine.trim());
                         }

                         // Parse JSON response
                         JSONObject jsonResponse = new JSONObject(response.toString());
                         JSONArray modelsArray = jsonResponse.getJSONArray("models");

                         // Extract model names
                         String[] modelNames = new String[modelsArray.length()];
                         for (int i = 0; i < modelsArray.length(); i++) {
                             modelNames[i] = modelsArray.getJSONObject(i).getString("name");
                         }
                         return modelNames;
                     }
                 } else {
                     throw new IOException("HTTP error code: " + responseCode);
                 }
             } catch (IOException e) {
                 e.printStackTrace();
                 return new String[]{"Error: " + e.getMessage()};
             }
         });
     }
    }
