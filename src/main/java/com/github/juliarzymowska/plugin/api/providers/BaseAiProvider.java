package com.github.juliarzymowska.plugin.api.providers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public abstract class BaseAiProvider implements AiProvider {

    protected static final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public CompletableFuture<String> analyzeError(String errorMessage, String apiKey) {

        // 1. Dziecko, daj mi gotowe zapytanie HTTP (z poprawnym JSONem, URLem i nagłówkami)
        HttpRequest request = buildHttpRequest(errorMessage, apiKey);

        // 2. Ja (klasa bazowa) zajmę się asynchronicznym wysłaniem tego w świat
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        // 3. Sukces! Dziecko, weź tego surowego JSONa i wyciągnij z niego to co ważne
                        return parseResponse(response.body());
                    }
                    throw new RuntimeException("API Error (HTTP " + response.statusCode() + "):\n" + response.body());
                });
    }

    // --- METODY ABSTRAKCYJNE (KAŻDY PROVIDER MUSI JE ZAIMPLEMENTOWAĆ PO SWOJEMU) ---

    protected abstract HttpRequest buildHttpRequest(String errorMessage, String apiKey);

    protected abstract String parseResponse(String rawJsonResponse);
}