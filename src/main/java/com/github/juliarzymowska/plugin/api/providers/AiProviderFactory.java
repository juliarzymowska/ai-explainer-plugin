package com.github.juliarzymowska.plugin.api.providers;

public class AiProviderFactory {
    public static AiProvider getProvider(String providerName) {
        if ("OpenAI".equalsIgnoreCase(providerName)) {
            return new OpenAiProvider();
        }
        // Domyślnie używamy Gemini (lub możemy rzucić wyjątek, jeśli nazwa jest nieznana)
        return new GeminiProvider();
    }
}