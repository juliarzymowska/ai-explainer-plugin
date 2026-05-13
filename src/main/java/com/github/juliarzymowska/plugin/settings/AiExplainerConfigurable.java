package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class AiExplainerConfigurable implements Configurable {

    private AiExplainerSettingsComponent settingsComponent;

    // CACHE: Pamięć podręczna wczytanych kluczy, aby nie blokować wątku UI
    private final Map<String, String> initialKeysCache = new HashMap<>();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() { return "AI Explainer"; }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new AiExplainerSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        boolean modified = !settingsComponent.getGeminiModel().equals(settings.geminiModel);

        // BŁYSKAWICZNE PORÓWNANIE: Używamy naszego cache w pamięci RAM, a nie PasswordSafe
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

    @Override
    public void apply() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        settings.geminiModel = settingsComponent.getGeminiModel();

        // Zapis do sejfu WYŁĄCZNIE przy kliknięciu Apply/OK
        Map<String, String> currentKeysInUI = settingsComponent.getApiKeys();
        currentKeysInUI.forEach(ApiKeyManager::saveKey);

        // Aktualizujemy cache po zapisie
        initialKeysCache.putAll(currentKeysInUI);
    }

    @Override
    public void reset() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        settingsComponent.setGeminiModel(settings.geminiModel);

        // Odczyt z sejfu WYŁĄCZNIE raz podczas ładowania widoku
        initialKeysCache.clear();
        for (String provider : AiExplainerSettingsComponent.PROVIDERS) {
            String key = ApiKeyManager.getKey(provider);
            initialKeysCache.put(provider, key != null ? key : "");
        }
        settingsComponent.setApiKeys(initialKeysCache);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}