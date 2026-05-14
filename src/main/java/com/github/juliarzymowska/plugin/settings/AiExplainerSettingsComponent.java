package com.github.juliarzymowska.plugin.settings;

import com.github.juliarzymowska.plugin.api.providers.AiProviderType;
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
    private final Map<AiProviderType, JBPasswordField> apiKeysFields = new HashMap<>();
    private final Map<AiProviderType, ComboBox<String>> modelDropdowns = new HashMap<>();

    public AiExplainerSettingsComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();

        // Dynamically build password fields AND model dropdowns for EVERY provider
        for (AiProviderType type : AiProviderType.values()) {
            // 1. Password Field
            JBPasswordField passwordField = new JBPasswordField();
            apiKeysFields.put(type, passwordField);
            builder.addLabeledComponent(new JBLabel(type.getDisplayName() + " API Key:"), passwordField, 1, false);

            // 2. Model Dropdown (if the provider has models defined)
            if (!type.getSupportedModels().isEmpty()) {
                ComboBox<String> dropdown = new ComboBox<>(type.getSupportedModels().toArray(new String[0]));
                modelDropdowns.put(type, dropdown);
                builder.addLabeledComponent(new JBLabel(type.getDisplayName() + " Model:"), dropdown, 1, false);
            }
            builder.addSeparator();
        }

        builder.addComponentFillVertically(new JPanel(), 0);
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
        apiKeysFields.forEach((type, field) -> keys.put(type.getDisplayName(), new String(field.getPassword())));
        return keys;
    }
    /**
     * Populates the dynamic password fields with the provided API keys.
     *
     * @param keys A map containing the provider names and their existing API keys.
     */
    public void setApiKeys(Map<String, String> keys) {
        apiKeysFields.forEach((type, field) -> {
            String key = keys.get(type.getDisplayName());
            if (key != null) field.setText(key);
        });
    }
    // New generic methods for models
    public Map<AiProviderType, String> getSelectedModels() {
        Map<AiProviderType, String> models = new HashMap<>();
        modelDropdowns.forEach((type, dropdown) -> models.put(type, (String) dropdown.getSelectedItem()));
        return models;
    }

    public void setSelectedModels(Map<AiProviderType, String> models) {
        modelDropdowns.forEach((type, dropdown) -> {
            String selected = models.get(type);
            if (selected != null) dropdown.setSelectedItem(selected);
        });
    }}