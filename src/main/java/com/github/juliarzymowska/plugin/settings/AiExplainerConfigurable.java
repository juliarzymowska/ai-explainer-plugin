package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AiExplainerConfigurable implements Configurable {

    private AiExplainerSettingsComponent settingsComponent;

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

    @Override
    public boolean isModified() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        boolean modified = !settingsComponent.getActiveProvider().equals(settings.activeProvider);
        modified |= !settingsComponent.getOpenAiApiKey().equals(settings.openAiApiKey);
        modified |= !settingsComponent.getGeminiApiKey().equals(settings.geminiApiKey);
        modified |= !settingsComponent.getGeminiModel().equals(settings.geminiModel);
        return modified;
    }

    @Override
    public void apply() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        settings.activeProvider = settingsComponent.getActiveProvider();
        settings.openAiApiKey = settingsComponent.getOpenAiApiKey();
        settings.geminiApiKey = settingsComponent.getGeminiApiKey();
        settings.geminiModel = settingsComponent.getGeminiModel();
    }

    @Override
    public void reset() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        settingsComponent.setActiveProvider(settings.activeProvider);
        settingsComponent.setOpenAiApiKey(settings.openAiApiKey);
        settingsComponent.setGeminiApiKey(settings.geminiApiKey);
        settingsComponent.setGeminiModel(settings.geminiModel);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}