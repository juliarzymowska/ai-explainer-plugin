package com.github.juliarzymowska.plugin.api.providers;

import java.util.concurrent.CompletableFuture;

/**
 * Defines the core contract for all AI service providers (e.g., Gemini, OpenAI).
 * Any new AI integration must implement this interface to ensure seamless compatibility
 * with the plugin's architecture.
 */
public interface AiProvider {

    /**
     * Asynchronously analyzes the provided error message and contextual source code
     * using the specific AI provider's API.
     * <p>
     * The method returns a {@link CompletableFuture} to ensure that the heavy network
     * request does not block the IDE's main UI thread (Event Dispatch Thread).
     *
     * @param errorMessage The raw error message, stack trace, or exception description selected by the user.
     * @param sourceCode   The full source code of the file where the error occurred, used to provide context to the LLM.
     *                     Can be empty or null if the context is unavailable.
     * @param apiKey       The authentication token (API key) required to authorize the request with the provider.
     * @return A {@link CompletableFuture} that, when completed, contains the AI's response
     * formatted as a JSON string containing 'errorSummary', 'rootCause', and 'suggestedFix'.
     */
    CompletableFuture<String> analyzeError(String errorMessage, String sourceCode, String apiKey);
}