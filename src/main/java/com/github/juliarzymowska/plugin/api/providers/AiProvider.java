package com.github.juliarzymowska.plugin.api.providers;

import java.util.concurrent.CompletableFuture;

public interface AiProvider {
    /**
     * Każdy dostawca AI musi zaimplementować tę metodę po swojemu.
     */
    CompletableFuture<String> analyzeError(String errorMessage, String apiKey);
}