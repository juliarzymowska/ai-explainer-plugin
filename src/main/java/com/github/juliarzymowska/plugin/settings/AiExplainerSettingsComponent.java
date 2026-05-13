package com.github.juliarzymowska.plugin.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiExplainerSettingsComponent {

    private final JPanel mainPanel;
    private final ComboBox<String> geminiModelDropdown = new ComboBox<>(new String[]{"gemini-3.1-flash-lite", "gemini-3.1-pro-preview"});

    // Lista wspieranych dostawców (dodanie tu "Claude" załatwi sprawę w przyszłości!)
    public static final List<String> PROVIDERS = List.of("OpenAI", "Gemini");

    // Mapa trzymająca dynamicznie wygenerowane pola haseł
    private final Map<String, JBPasswordField> apiKeysFields = new HashMap<>();

    public AiExplainerSettingsComponent() {
        FormBuilder builder = FormBuilder.createFormBuilder();

        // Dynamiczne budowanie pól dla każdego providera
        for (String provider : PROVIDERS) {
            JBPasswordField passwordField = new JBPasswordField();
            apiKeysFields.put(provider, passwordField);
            builder.addLabeledComponent(new JBLabel(provider + " API Key:"), passwordField, 1, false);
        }

        builder.addSeparator()
                .addLabeledComponent(new JBLabel("Gemini Model:"), geminiModelDropdown, 1, false)
                .addComponentFillVertically(new JPanel(), 0);

        mainPanel = builder.getPanel();
    }

    public JPanel getPanel() { return mainPanel; }
    public JComponent getPreferredFocusedComponent() { return apiKeysFields.get(PROVIDERS.get(0)); }

    // Dwie potężne metody zamiast kilkunastu getterów i setterów
    public Map<String, String> getApiKeys() {
        Map<String, String> keys = new HashMap<>();
        apiKeysFields.forEach((provider, field) -> keys.put(provider, new String(field.getPassword())));
        return keys;
    }

    public void setApiKeys(Map<String, String> keys) {
        keys.forEach((provider, key) -> {
            if (apiKeysFields.containsKey(provider)) {
                apiKeysFields.get(provider).setText(key);
            }
        });
    }

    // Ustawienia bezpieczne
    public String getGeminiModel() { return (String) geminiModelDropdown.getSelectedItem(); }
    public void setGeminiModel(String model) { geminiModelDropdown.setSelectedItem(model); }
}