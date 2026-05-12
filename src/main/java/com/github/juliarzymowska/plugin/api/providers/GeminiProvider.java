package com.github.juliarzymowska.plugin.api.providers;

import com.github.juliarzymowska.plugin.api.PromptTemplates;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpRequest;

public class GeminiProvider extends BaseAiProvider {

    private static final Gson gson = new Gson();

    @Override
    protected HttpRequest buildHttpRequest(String errorMessage, String sourceCode, String apiKey) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + apiKey;

        // Safety check in case source code is empty or null
        String finalSourceCode = (sourceCode != null && !sourceCode.trim().isEmpty())
                ? sourceCode
                : "No source code provided. Analyze based on the error message alone.";

        // Wstrzykujemy OBA parametry do prompta
        String formattedPrompt = String.format(PromptTemplates.ANALYZE_ERROR_PROMPT, errorMessage, finalSourceCode);

        // Czyste wywołanie wydzielonej metody
        JsonObject requestBody = buildRequestBody(formattedPrompt);

        return HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
    }

    // Wyciągnięta logika budowania JSON-a
    private JsonObject buildRequestBody(String formattedPrompt) {
        JsonObject part = new JsonObject();
        part.addProperty("text", formattedPrompt);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.2);
        generationConfig.addProperty("responseMimeType", "application/json");
        requestBody.add("generationConfig", generationConfig);

        return requestBody;
    }

    @Override
    protected String parseResponse(String rawJsonResponse) {
        JsonObject jsonResponse = gson.fromJson(rawJsonResponse, JsonObject.class);
        return jsonResponse.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }
}