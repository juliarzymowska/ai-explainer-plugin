package com.github.juliarzymowska.plugin.api.providers;

import java.util.List;

/**
 * Single source of truth for supported AI providers and their models.
 * Eliminates the use of "magic strings" across the application.
 */
public enum AiProviderType {
    GEMINI("Gemini", List.of("gemini-3.1-flash-lite", "gemini-3.1-pro-preview")),
    OPENAI("OpenAI", List.of("gpt-4o-mini", "gpt-4o"));

    private final String displayName;
    private final List<String> supportedModels;

    AiProviderType(String displayName, List<String> supportedModels) {
        this.displayName = displayName;
        this.supportedModels = supportedModels;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getSupportedModels() {
        return supportedModels;
    }

    /**
     * Resolves the Enum instance from the UI display name.
     */
    public static AiProviderType fromDisplayName(String name) {
        for (AiProviderType type : values()) {
            if (type.displayName.equals(name)) {
                return type;
            }
        }
        return GEMINI; // Default fallback
    }
}