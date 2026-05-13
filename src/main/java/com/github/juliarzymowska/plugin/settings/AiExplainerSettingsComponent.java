package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The visual representation (Swing UI) of the plugin's settings page.
 * <p>
 * This class isolates the raw UI construction from the configuration logic. It dynamically
 * builds password fields for all supported AI providers and exposes high-level methods
 * for the {@link AiExplainerConfigurable} controller to read and write state.
 */
public class AiExplainerSettingsComponent {

    private final JPanel mainPanel;
    private final ComboBox<String> geminiModelDropdown = new ComboBox<>(new String[]{"gemini-3.1-flash-lite", "gemini-3.1-pro-preview"});

    /**
     * A centralized list of supported AI providers.
     * Adding a new provider here (e.g., "Claude") will automatically generate the corresponding UI fields.
     */
    public static final List<String> PROVIDERS = List.of("OpenAI", "Gemini");

    /** A map associating provider names with their respective secure password input fields. */
    private final Map<String, JBPasswordField> apiKeysFields = new HashMap<>();

    public AiExplainerSettingsComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();

        // Dynamically build password fields for every supported provider
        for (String provider : PROVIDERS) {
            JBPasswordField passwordField = new JBPasswordField();
            apiKeysFields.put(provider, passwordField);
            builder.addLabeledComponent(new JBLabel(provider + " API Key:"), passwordField, 1, false);
        }

        builder.addSeparator()
                .addLabeledComponent(new JBLabel("Gemini model:"), geminiModelDropdown, 1, false)
                .addComponentFillVertically(new JPanel(), 0);

        mainPanel = builder.getPanel();
    }

    public JPanel getPanel() { return mainPanel; }

//    public JComponent getPreferredFocusedComponent() { return apiKeysFields.get(PROVIDERS.get(0)); }

    /**
     * Extracts the current plain-text API keys from all dynamic password fields.
     *
     * @return A map containing the provider names as keys and their corresponding API keys as values.
     */
    public Map<String, String> getApiKeys() {
        Map<String, String> keys = new HashMap<>();
        apiKeysFields.forEach((provider, field) -> keys.put(provider, new String(field.getPassword())));
        return keys;
    }

    /**
     * Populates the dynamic password fields with the provided API keys.
     *
     * @param keys A map containing the provider names and their existing API keys.
     */
    public void setApiKeys(Map<String, String> keys) {
        keys.forEach((provider, key) -> {
            if (apiKeysFields.containsKey(provider)) {
                apiKeysFields.get(provider).setText(key);
            }
        });
    }

    public String getGeminiModel() { return (String) geminiModelDropdown.getSelectedItem(); }
    public void setGeminiModel(String model) { geminiModelDropdown.setSelectedItem(model); }
}