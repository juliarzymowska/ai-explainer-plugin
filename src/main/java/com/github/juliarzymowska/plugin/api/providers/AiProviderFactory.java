package com.github.juliarzymowska.plugin.api.providers;

/**
 * A factory class responsible for instantiating the appropriate {@link AiProvider}
 * implementation based on the user's plugin configuration.
 * <p>
 * This class abstracts the creation logic, allowing the rest of the application
 * to remain agnostic about the specific concrete classes (e.g., OpenAiProvider, GeminiProvider).
 */
public class AiProviderFactory {

    /**
     * Creates and returns an instance of an {@link AiProvider} corresponding to the
     * specified provider name.
     * <p>
     * Currently supports "OpenAI". If the provided name does not match any explicitly
     * defined provider, the factory defaults to returning a {@link GeminiProvider}.
     *
     * @param type The name of the AI service provider (e.g., "OpenAI", "Gemini")
     *                     selected by the user in the plugin settings.
     * @return An instance of the requested {@link AiProvider}.
     */
    public static AiProvider getProvider(AiProviderType type) {
        if (type == AiProviderType.OPENAI) {
            return new OpenAiProvider();
        }
        // Default fallback
        return new GeminiProvider();
    }}