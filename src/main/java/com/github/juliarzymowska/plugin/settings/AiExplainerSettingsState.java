package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.github.juliarzymowska.plugin.settings.AiExplainerSettingsState",
        storages = @Storage("AiExplainerSettings.xml")
)
public class AiExplainerSettingsState implements PersistentStateComponent<AiExplainerSettingsState> {
    public String apiKey = "";

    public String aiProvider = "Gemini";

    public static AiExplainerSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AiExplainerSettingsState.class);
    }

    @Nullable
    @Override
    public AiExplainerSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AiExplainerSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}