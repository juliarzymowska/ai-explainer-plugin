package com.github.juliarzymowska.plugin.api.providers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract base class for all AI providers that handles the boilerplate of asynchronous HTTP communication.
 * <p>
 * This class employs the Template Method design pattern: it defines the skeleton of the API call lifecycle
 * (sending the request, handling the HTTP status, and managing asynchronous execution) while delegating the
 * provider-specific request construction and response parsing to its concrete subclasses.
 */
public abstract class BaseAiProvider implements AiProvider {

    /**
     * A shared, thread-safe HTTP client instance used across all AI provider implementations.
     * Reusing a single client optimizes memory and performance by taking advantage of connection pooling.
     */
    protected static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Orchestrates the asynchronous API call to the respective AI service.
     *
     * @param errorMessage The raw error message or stack trace to be analyzed.
     * @param sourceCode   The contextual source code related to the error.
     * @param apiKey       The authentication key for the AI provider.
     * @return A {@link CompletableFuture} containing the parsed JSON response.
     * @throws RuntimeException if the HTTP response status code is not 200 (OK).
     */
    @Override
    public CompletableFuture<String> analyzeError(String errorMessage, String sourceCode, String apiKey) {
        HttpRequest request = buildHttpRequest(errorMessage, sourceCode, apiKey);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseResponse(response.body());
                    }
                    throw new RuntimeException("API Error (HTTP " + response.statusCode() + "):\n" + response.body());
                });
    }

    /**
     * Constructs the provider-specific HTTP request, including setting the target URL,
     * authorization headers, and formatting the JSON payload.
     *
     * @param errorMessage The raw error message to include in the AI prompt.
     * @param sourceCode   The contextual source code to include in the AI prompt.
     * @param apiKey       The API key used for authorization.
     * @return A fully constructed {@link HttpRequest} ready to be dispatched.
     */
    protected abstract HttpRequest buildHttpRequest(String errorMessage, String sourceCode, String apiKey);

    /**
     * Parses the raw JSON response returned by the AI provider's API into the standardized
     * JSON string format expected by the plugin's UI renderer.
     *
     * @param rawJsonResponse The raw string body of the HTTP response.
     * @return A standardized JSON string containing the 'errorSummary', 'rootCause', and 'suggestedFix' fields.
     */
    protected abstract String parseResponse(String rawJsonResponse);
}