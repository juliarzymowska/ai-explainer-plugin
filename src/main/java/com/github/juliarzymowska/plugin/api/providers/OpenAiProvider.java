package com.github.juliarzymowska.plugin.api.providers;

import com.github.juliarzymowska.plugin.api.PromptTemplates;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpRequest;

public class OpenAiProvider extends BaseAiProvider {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Gson gson = new Gson();

    @Override
    protected HttpRequest buildHttpRequest(String errorMessage, String sourceCode, String apiKey) {

        // Safety check in case source code is empty or null
        String finalSourceCode = (sourceCode != null && !sourceCode.trim().isEmpty())
                ? sourceCode
                : "No source code provided. Analyze based on the error message alone.";

        // Wstrzykujemy OBA parametry do prompta
        String formattedPrompt = String.format(PromptTemplates.ANALYZE_ERROR_PROMPT, errorMessage, finalSourceCode);

        JsonObject requestBody = buildRequestBody(formattedPrompt);

        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
    }

    // Wyciągnięta logika budowania JSON-a
    private JsonObject buildRequestBody(String formattedPrompt) {
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are an expert Java developer and debugging assistant.");

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", formattedPrompt);

        JsonArray messages = new JsonArray();
        messages.add(systemMessage);
        messages.add(userMessage);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.2);

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        requestBody.add("response_format", responseFormat);

        return requestBody;
    }

    @Override
    protected String parseResponse(String rawJsonResponse) {
        JsonObject jsonResponse = gson.fromJson(rawJsonResponse, JsonObject.class);
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }
}