package com.github.juliarzymowska.plugin.api.providers;

import java.util.concurrent.CompletableFuture;

public interface AiProvider {
    // Added sourceCode parameter
    CompletableFuture<String> analyzeError(String errorMessage, String sourceCode, String apiKey);
}