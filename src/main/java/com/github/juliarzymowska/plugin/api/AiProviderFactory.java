package com.github.juliarzymowska.plugin.api;

import com.github.juliarzymowska.plugin.api.providers.AiProvider;
import com.github.juliarzymowska.plugin.api.providers.GeminiProvider;
import com.github.juliarzymowska.plugin.api.providers.OpenAiProvider;

public class AiProviderFactory {
    public static AiProvider getProvider(String providerName) {
        if ("OpenAI".equalsIgnoreCase(providerName)) {
            return new OpenAiProvider();
        }
        // Domyślnie używamy Gemini (lub możemy rzucić wyjątek, jeśli nazwa jest nieznana)
        return new GeminiProvider();
    }
}