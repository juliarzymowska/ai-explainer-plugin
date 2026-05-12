package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class AiExplainerSettingsComponent {

    private final JPanel mainPanel;
    private final ComboBox<String> activeProviderDropdown = new ComboBox<>(new String[]{"Gemini", "OpenAI"});
    private final JBPasswordField openAiKeyText = new JBPasswordField();
    private final JBPasswordField geminiKeyText = new JBPasswordField();
    private final ComboBox<String> geminiModelDropdown = new ComboBox<>(new String[]{"gemini-3.1-flash-lite", "gemini-3.1-pro"});

    public AiExplainerSettingsComponent() {
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Active AI Provider:"), activeProviderDropdown, 1, false)
                .addSeparator()
                .addLabeledComponent(new JBLabel("OpenAI API Key:"), openAiKeyText, 1, false)
                .addLabeledComponent(new JBLabel("Gemini API Key:"), geminiKeyText, 1, false)
                .addLabeledComponent(new JBLabel("Gemini model:"), geminiModelDropdown, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return activeProviderDropdown;
    }

    // Getters
    public String getActiveProvider() {
        return (String) activeProviderDropdown.getSelectedItem();
    }

    public String getOpenAiApiKey() {
        return new String(openAiKeyText.getPassword());
    }

    public String getGeminiApiKey() {
        return new String(geminiKeyText.getPassword());
    }

    public String getGeminiModel() {
        return (String) geminiModelDropdown.getSelectedItem();
    }

    // Setters
    public void setActiveProvider(String provider) {
        activeProviderDropdown.setSelectedItem(provider);
    }

    public void setOpenAiApiKey(String key) {
        openAiKeyText.setText(key);
    }

    public void setGeminiApiKey(String key) {
        geminiKeyText.setText(key);
    }

    public void setGeminiModel(String model) {
        geminiModelDropdown.setSelectedItem(model);
    }
}