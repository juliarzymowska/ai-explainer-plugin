package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AiExplainerConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBPasswordField apiKeyField;
    private ComboBox<String> providerComboBox;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "AI Console Log Explainer";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        apiKeyField = new JBPasswordField();

        providerComboBox = new ComboBox<>(new String[]{"Gemini", "OpenAI"});

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("AI provider: "), providerComboBox, 1, false)
                .addLabeledComponent(new JBLabel("API key: "), apiKeyField, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();

        String currentKey = new String(apiKeyField.getPassword());
        String currentProvider = (String) providerComboBox.getSelectedItem();

        boolean isKeyModified = !currentKey.equals(settings.apiKey);
        boolean isProviderModified = currentProvider != null && !currentProvider.equals(settings.aiProvider);

        return isKeyModified || isProviderModified;
    }

    @Override
    public void apply() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        settings.apiKey = new String(apiKeyField.getPassword());

        if (providerComboBox.getSelectedItem() != null) {
            settings.aiProvider = (String) providerComboBox.getSelectedItem();
        }
    }

    @Override
    public void reset() {
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();
        apiKeyField.setText(settings.apiKey);

        providerComboBox.setSelectedItem(settings.aiProvider);
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        apiKeyField = null;
        providerComboBox = null;
    }
}