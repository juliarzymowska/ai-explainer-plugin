package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProvider;
import com.github.juliarzymowska.plugin.api.AiProviderFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import com.github.juliarzymowska.plugin.settings.AiExplainerSettingsState;

import javax.swing.*;
import java.awt.*;

public class AiToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 1. Tworzymy główny panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 2. Tworzymy pole tekstowe, w którym pokażemy zczytany błąd
        JTextArea consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false); // Użytkownik ma tylko czytać, nie pisać
        consoleTextArea.setText("Tutaj pojawi się błąd z konsoli po kliknięciu 'Zczytaj'...");
        mainPanel.add(new JBScrollPane(consoleTextArea), BorderLayout.CENTER);

        // 3. Tworzymy panel na przyciski (na dole)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton fetchLogsButton = new JButton("1. Fetch Console Logs");
        JButton sendToAiButton = new JButton("2. Analyze with AI");
        sendToAiButton.setEnabled(false); // Wyłączony, dopóki nie mamy logów

        buttonPanel.add(fetchLogsButton);
        buttonPanel.add(sendToAiButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- LOGIKA PRZYCISKÓW ---

        // Logika przycisku Zczytaj
        fetchLogsButton.addActionListener(e -> {
            // Szukamy aktywnej konsoli w projekcie
            RunContentDescriptor activeConsole = ExecutionManager.getInstance(project).getContentManager().getSelectedContent();

            if (activeConsole != null && activeConsole.getExecutionConsole() != null) {
                // Konwersja obiektu konsoli do stringa (magia SDK)
                // W prawdziwym projekcie używamy tu strumieni, ale do MVP to wystarczy
                String consoleText = activeConsole.getExecutionConsole().getComponent().getAccessibleContext().getAccessibleName();

                // Czasami ten obiekt jest pusty, jeśli tak, używamy hacka:
                if (consoleText == null || consoleText.isEmpty()) {
                    consoleText = "Skopiuj i wklej tu błąd, auto-detekcja w tym oknie nie zadziałała. \n (Ale tu zrobimy HTTP request do AI!)";
                    consoleTextArea.setEditable(true);
                }

                consoleTextArea.setText(consoleText);
                sendToAiButton.setEnabled(true); // Włączamy przycisk AI
            } else {
                consoleTextArea.setText("Nie znaleziono aktywnej konsoli. Uruchom najpierw jakiś program!");
            }
        });

        // Logika przycisku Analizuj (Tutaj podepniemy API!)
        sendToAiButton.addActionListener(e -> {
            String textToSend = consoleTextArea.getText();
            String apiKey = AiExplainerSettingsState.getInstance().apiKey;
            String providerName = AiExplainerSettingsState.getInstance().aiProvider;

            if (apiKey == null || apiKey.trim().isEmpty()) {
                consoleTextArea.setText("Brak klucza API! Wejdź w Ustawienia.");
                return;
            }

            sendToAiButton.setEnabled(false);
            consoleTextArea.setText(consoleTextArea.getText() + "\n\n=== AI THINKING ===");

            // 1. Fabryka daje nam odpowiedni obiekt
            AiProvider aiProvider = AiProviderFactory.getProvider(providerName);

            // 2. Wywołujemy zunifikowaną metodę
            aiProvider.analyzeError(textToSend, apiKey)
                    .thenAccept(jsonResponseString -> {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                // Parsujemy odpowiedź AI, która dzięki naszemu promptowi jest teraz poprawnym JSONem!
                                JsonObject aiAnswerJson = new Gson().fromJson(jsonResponseString, JsonObject.class);

                                String summary = aiAnswerJson.get("errorSummary").getAsString();
                                String cause = aiAnswerJson.get("rootCause").getAsString();
                                String fix = aiAnswerJson.get("suggestedFix").getAsString();

                                // Formatujemy ładny tekst w oknie
                                String formattedOutput = textToSend +
                                        "\n\n=== AI ANALYSIS ===\n" +
                                        "🛑 SUMMARY: " + summary + "\n\n" +
                                        "🔍 CAUSE: " + cause + "\n\n" +
                                        "🛠️ FIX: " + fix;

                                consoleTextArea.setText(formattedOutput);
                                sendToAiButton.setEnabled(true);
                            } catch (Exception parseEx) {
                                // Fallback, na wypadek gdyby model zignorował instrukcję i wypisał zwykły tekst
                                consoleTextArea.setText(textToSend + "\n\n=== AI RESPONSE ===\n" + jsonResponseString);
                                sendToAiButton.setEnabled(true);
                            }
                        });
                    }).exceptionally(ex -> {
                        SwingUtilities.invokeLater(() -> {
                            consoleTextArea.setText(textToSend + "\n\n=== BŁĄD WTYCZKI ===\n" + ex.getMessage());
                            sendToAiButton.setEnabled(true);
                        });
                        return null;
                    });
        });

        // 4. Dodanie naszego panelu do środowiska IntelliJ
        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}