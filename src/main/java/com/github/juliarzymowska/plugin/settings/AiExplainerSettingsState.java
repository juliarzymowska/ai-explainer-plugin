package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the persistent, non-sensitive state of the plugin's settings.
 * <p>
 * This service is automatically serialized by the IntelliJ Platform into an XML file
 * (AiExplainerPlugin.xml). Sensitive data, such as API keys, are strictly excluded
 * from this class and are handled entirely by the {@link ApiKeyManager}.
 */
@State(
        name = "com.github.juliarzymowska.plugin.settings.AiExplainerSettingsState",
        storages = @Storage("AiExplainerPlugin.xml")
)
public class AiExplainerSettingsState implements PersistentStateComponent<AiExplainerSettingsState> {

    /** The default Gemini model selected by the user. */
    public String geminiModel = "gemini-3.1-flash-lite";

    /**
     * Retrieves the application-level instance of this settings state.
     *
     * @return The singleton instance of {@link AiExplainerSettingsState}.
     */
    public static AiExplainerSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AiExplainerSettingsState.class);
    }

    @Nullable
    @Override
    public AiExplainerSettingsState getState() { return this; }

    @Override
    public void loadState(AiExplainerSettingsState state) { XmlSerializerUtil.copyBean(state, this); }
}