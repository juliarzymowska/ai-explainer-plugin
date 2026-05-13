package com.github.juliarzymowska.plugin.settings;

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
     * <p>
     * It utilizes the {@link #initialKeysCache} to perform a lightning-fast comparison
     * of the API keys without querying the operating system's credential store.
     *
     * @return {@code true} if the UI state differs from the saved state; {@code false} otherwise.
     */
    @Override
    public boolean isModified() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        boolean modified = !settingsComponent.getGeminiModel().equals(settings.geminiModel);

        Map<String, String> currentKeysInUI = settingsComponent.getApiKeys();
        for (String provider : AiExplainerSettingsComponent.PROVIDERS) {
            String initialKey = initialKeysCache.getOrDefault(provider, "");
            String uiKey = currentKeysInUI.getOrDefault(provider, "");

            if (!uiKey.equals(initialKey)) {
                modified = true;
                break;
            }
        }
        return modified;
    }

    /**
     * Persists the user's changes.
     * <p>
     * Standard configuration options are saved to the plugin's XML state, while sensitive
     * data (API keys) are securely pushed to the native keychain via {@link ApiKeyManager}.
     * The in-memory cache is subsequently updated to reflect the newly saved state.
     */
    @Override
    public void apply() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        settings.geminiModel = settingsComponent.getGeminiModel();

        Map<String, String> currentKeysInUI = settingsComponent.getApiKeys();
        currentKeysInUI.forEach(ApiKeyManager::saveKey);

        initialKeysCache.putAll(currentKeysInUI);
    }

    /**
     * Resets the UI components to match the currently stored state.
     * <p>
     * This method is called once when the settings page is opened, or when the user clicks
     * the "Reset" button. It performs the heavy lifting of reading from the system keychain
     * exactly once and populates both the UI and the local cache.
     */
    @Override
    public void reset() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        settingsComponent.setGeminiModel(settings.geminiModel);

        initialKeysCache.clear();
        for (String provider : AiExplainerSettingsComponent.PROVIDERS) {
            String key = ApiKeyManager.getKey(provider);
            initialKeysCache.put(provider, key != null ? key : "");
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