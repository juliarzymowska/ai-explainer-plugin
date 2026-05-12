package com.github.juliarzymowska.plugin.api.providers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public abstract class BaseAiProvider implements AiProvider {

    protected static final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public CompletableFuture<String> analyzeError(String errorMessage, String sourceCode, String apiKey) {

        // Passing both errorMessage and sourceCode to the concrete implementations
        HttpRequest request = buildHttpRequest(errorMessage, sourceCode, apiKey);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseResponse(response.body());
                    }
                    throw new RuntimeException("API Error (HTTP " + response.statusCode() + "):\n" + response.body());
                });
    }

    // Updated abstract method signature
    protected abstract HttpRequest buildHttpRequest(String errorMessage, String sourceCode, String apiKey);

    protected abstract String parseResponse(String rawJsonResponse);
}