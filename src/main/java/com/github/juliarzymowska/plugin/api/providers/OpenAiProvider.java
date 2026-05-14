package com.github.juliarzymowska.plugin.api.providers;

import com.github.juliarzymowska.plugin.api.PromptTemplates;
import com.github.juliarzymowska.plugin.settings.AiExplainerSettingsState;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpRequest;

/**
 * Concrete implementation of the {@link BaseAiProvider} for the OpenAI API.
 * <p>
 * This class handles the construction of chat completion requests, including setting up
 * the required system/user message roles, applying Bearer token authorization, and
 * parsing the OpenAI-specific response structure.
 */
public class OpenAiProvider extends BaseAiProvider {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Gson gson = new Gson();

    /**
     * Constructs the HTTP request tailored for the OpenAI API.
     * <p>
     * It formats the prompt with the error message and source code, providing a fallback
     * message if the source code context is missing. It also securely attaches the
     * provided API key as a Bearer token in the Authorization header.
     *
     * @param errorMessage The raw error message or stack trace.
     * @param sourceCode   The contextual source code (can be null or empty).
     * @param apiKey       The OpenAI API key used for Bearer authentication.
     * @return A configured {@link HttpRequest} ready to be sent to OpenAI's servers.
     */
    @Override
    protected HttpRequest buildHttpRequest(String errorMessage, String sourceCode, String apiKey) {
        // 1. Fetch the model dynamically from the centralized settings state
        String selectedModel = AiExplainerSettingsState.getInstance()
                .selectedModels.getOrDefault(AiProviderType.OPENAI.name(), AiProviderType.OPENAI.getSupportedModels().getFirst());

        String finalSourceCode = (sourceCode != null && !sourceCode.trim().isEmpty())
                ? sourceCode
                : "No source code provided. Analyze based on the error message alone.";

        String formattedPrompt = String.format(PromptTemplates.ANALYZE_ERROR_PROMPT, errorMessage, finalSourceCode);

        // 2. Pass the selected model into the body builder
        JsonObject requestBody = buildRequestBody(formattedPrompt, selectedModel);

        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
    }

    /**
     * Constructs the JSON payload required by the OpenAI chat completions endpoint.
     * <p>
     * This includes setting up a "system" message to define the AI's persona, a "user"
     * message containing the actual prompt, and configuring parameters such as the
     * dynamically selected model and low temperature for deterministic responses.
     * It also explicitly forces the response format to be a JSON object.
     *
     * @param formattedPrompt The final string prompt to be sent to the model.
     * @param selectedModel   The specific OpenAI model selected by the user (e.g., "gpt-4o").
     * @return A {@link JsonObject} representing the entire HTTP request body.
     */
    private JsonObject buildRequestBody(String formattedPrompt, String selectedModel) {
        JsonArray messages = getMessages(formattedPrompt);

        JsonObject requestBody = new JsonObject();
        // 4. Inject the dynamically selected model
        requestBody.addProperty("model", selectedModel);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.2);

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        requestBody.add("response_format", responseFormat);

        return requestBody;
    }

    private static @NotNull JsonArray getMessages(String formattedPrompt) {
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        // 3. Utilize the centralized PromptTemplates class for the persona
        systemMessage.addProperty("content", PromptTemplates.SYSTEM_ROLE_PROMPT);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", formattedPrompt);

        JsonArray messages = new JsonArray();
        messages.add(systemMessage);
        messages.add(userMessage);
        return messages;
    }

    /**
     * Parses the JSON response returned by the OpenAI API to extract the generated text content.
     * <p>
     * OpenAI returns the generated message deeply nested within a "choices" array.
     *
     * @param rawJsonResponse The raw JSON string returned by the HTTP response body.
     * @return The extracted text generated by the OpenAI model.
     */
    @Override
    protected String parseResponse(String rawJsonResponse) {
        JsonObject jsonResponse = gson.fromJson(rawJsonResponse, JsonObject.class);
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }
}