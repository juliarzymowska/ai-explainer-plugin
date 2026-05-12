package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProvider;
import com.github.juliarzymowska.plugin.api.providers.AiProviderFactory;
import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.github.juliarzymowska.plugin.settings.AiExplainerSettingsState;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class AiToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JTextArea consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setText("Select an error in the console or editor -> Right Click -> 'AI Explainer: Analyze Error'");
        mainPanel.add(new JBScrollPane(consoleTextArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton sendToAiButton = new JButton("Analyze with AI");
        sendToAiButton.setEnabled(false);

        // Mała etykieta informująca, czy załączono kontekst z kodem
        JLabel contextLabel = new JLabel("Context: None");
        contextLabel.setForeground(JBColor.GRAY);

        buttonPanel.add(sendToAiButton);
        buttonPanel.add(contextLabel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- NASŁUCHIWANIE DANYCH Z PRAWIEGO PRZYCISKU ---
        SharedStateService sharedState = project.getService(SharedStateService.class);

        sharedState.setOnDataUpdatedCallback(() -> {
            consoleTextArea.setText(sharedState.getErrorMessage());

            // Sprawdzamy, czy wstrzyknięto kod
            if (sharedState.getSourceCode() != null && !sharedState.getSourceCode().isEmpty()) {
                contextLabel.setText("Context: Source code attached \u2705"); // Znacznik zielonego ptaszka
                contextLabel.setForeground(new Color(0, 150, 0));
            } else {
                contextLabel.setText("Context: No source code \u274C"); // Znacznik czerwonego X
                contextLabel.setForeground(Color.RED);
            }
            sendToAiButton.setEnabled(true);
        });

        // --- LOGIKA ANALIZY ---
        sendToAiButton.addActionListener(e -> {
            // Pobieramy obie wartości z naszego serwisu
            String errorMessage = sharedState.getErrorMessage();
            String sourceCode = sharedState.getSourceCode();

            String apiKey = AiExplainerSettingsState.getInstance().apiKey;
            String providerName = AiExplainerSettingsState.getInstance().aiProvider;

            if (apiKey == null || apiKey.trim().isEmpty()) {
                consoleTextArea.setText("API Key is missing! Please configure it in Settings.");
                return;
            }

            sendToAiButton.setEnabled(false);
            consoleTextArea.setText(errorMessage + "\n\n=== AI THINKING... ===");

            AiProvider aiProvider = AiProviderFactory.getProvider(providerName);

            // TUTAJ NAPRAWIONO BŁĄD: Przekazujemy wszystkie 3 argumenty
            aiProvider.analyzeError(errorMessage, sourceCode, apiKey)
                    .thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                JsonObject aiAnswerJson = new Gson().fromJson(response, JsonObject.class);
                                String summary = aiAnswerJson.get("errorSummary").getAsString();
                                String cause = aiAnswerJson.get("rootCause").getAsString();
                                String fix = aiAnswerJson.get("suggestedFix").getAsString();

                                String formattedOutput = errorMessage +
                                        "\n\n=== AI ANALYSIS ===\n" +
                                        "\uD83D\uDED1 SUMMARY: " + summary + "\n\n" + // Stop sign
                                        "\uD83D\uDD0D CAUSE: " + cause + "\n\n" + // Magnifying glass
                                        "\uD83D\uDEE0\uFE0F FIX: " + fix; // Tools

                                consoleTextArea.setText(formattedOutput);
                            } catch (Exception parseEx) {
                                // Fallback w razie dziwnej odpowiedzi
                                consoleTextArea.setText(errorMessage + "\n\n=== AI RESPONSE ===\n" + response);
                            }
                            sendToAiButton.setEnabled(true);
                        });
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(() -> {
                            consoleTextArea.setText(errorMessage + "\n\n=== PLUGIN ERROR ===\n" + ex.getMessage());
                            sendToAiButton.setEnabled(true);
                        });
                        return null;
                    });
        });

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}