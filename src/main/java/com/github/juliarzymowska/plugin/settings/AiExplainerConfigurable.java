package com.github.juliarzymowska.plugin.settings;

import com.github.juliarzymowska.plugin.api.providers.AiProviderType;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller class for the plugin's Settings/Preferences dialog page.
 * <p>
 * It implements the {@link Configurable} interface to integrate with the IntelliJ IDE
 * settings menu. This class acts as the bridge between the UI components ({@link AiExplainerSettingsComponent})
 * and the persistent storage ({@link AiExplainerSettingsState} and {@link ApiKeyManager}).
 */
public class AiExplainerConfigurable implements Configurable {

    private AiExplainerSettingsComponent settingsComponent;

    /**
     * An in-memory cache of the initially loaded API keys.
     * <p>
     * Accessing the system's native keychain (via PasswordSafe) can be a slow operation.
     * Since the IDE frequently calls the {@link #isModified()} method on the UI thread (EDT)
     * to enable/disable the "Apply" button, reading from PasswordSafe directly would cause UI freezes.
     * This cache ensures instantaneous comparisons.
     */
    private final Map<String, String> initialKeysCache = new HashMap<>();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "AI Explainer";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new AiExplainerSettingsComponent();
        return settingsComponent.getPanel();
    }

    /**
     * Checks if the user has modified any settings in the UI compared to the stored state.
     *
     * @return {@code true} if the UI state differs from the saved state; {@code false} otherwise.
     */
    @Override
    public boolean isModified() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();

        // 1. Check if any selected models have been modified
        Map<AiProviderType, String> uiModels = settingsComponent.getSelectedModels();
        for (Map.Entry<AiProviderType, String> entry : uiModels.entrySet()) {
            String savedModel = settings.selectedModels.get(entry.getKey().name());
            if (!entry.getValue().equals(savedModel)) {
                return true;
            }
        }

        // 2. Check if any API keys have been modified
        Map<String, String> currentKeysInUI = settingsComponent.getApiKeys();
        for (AiProviderType type : AiProviderType.values()) {
            String providerName = type.getDisplayName();
            String initialKey = initialKeysCache.getOrDefault(providerName, "");
            String uiKey = currentKeysInUI.getOrDefault(providerName, "");

            if (!uiKey.equals(initialKey)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Persists the user's changes to the XML state and the native keychain.
     */
    @Override
    public void apply() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();

        // Save selected models
        Map<AiProviderType, String> uiModels = settingsComponent.getSelectedModels();
        uiModels.forEach((type, model) -> settings.selectedModels.put(type.name(), model));

        // Save API keys
        Map<String, String> currentKeysInUI = settingsComponent.getApiKeys();
        currentKeysInUI.forEach(ApiKeyManager::saveKey);

        // Update the cache
        initialKeysCache.clear();
        initialKeysCache.putAll(currentKeysInUI);
    }

    /**
     * Resets the UI components to match the currently stored state.
     */
    @Override
    public void reset() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();

        // Load saved models into UI
        Map<AiProviderType, String> modelsToSet = new HashMap<>();
        for (AiProviderType type : AiProviderType.values()) {
            String savedModel = settings.selectedModels.get(type.name());
            if (savedModel != null) {
                modelsToSet.put(type, savedModel);
            }
        }
        settingsComponent.setSelectedModels(modelsToSet);

        // Load API keys into UI and Cache
        initialKeysCache.clear();
        for (AiProviderType type : AiProviderType.values()) {
            String providerName = type.getDisplayName();
            String key = ApiKeyManager.getKey(providerName);
            initialKeysCache.put(providerName, key != null ? key : "");
        }
        settingsComponent.setApiKeys(initialKeysCache);
    }

    /**
     * Disposes of the UI resources to free up memory when the Settings dialog is closed.
     */
    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}