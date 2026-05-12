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
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.jcef.JBCefOsrHandlerBrowser;
import org.jetbrains.annotations.NotNull;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;

public class AiToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // GÓRNY PANEL: Błąd i kontrolki
        JPanel topPanel = new JPanel(new BorderLayout());
        JTextArea errorTextArea = new JTextArea("Select an error -> Right Click -> 'AI Explainer: Analyze Error'");
        errorTextArea.setEditable(false);
        topPanel.add(new JBScrollPane(errorTextArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton sendToAiButton = new JButton("Analyze with AI");
        sendToAiButton.setEnabled(false);
        JLabel contextLabel = new JLabel("Context: None");
        contextLabel.setForeground(JBColor.GRAY);

        buttonPanel.add(sendToAiButton);
        buttonPanel.add(contextLabel);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // DOLNY PANEL: Odpowiedź AI (obsługująca HTML dla lepszej czytelności!)
        JEditorPane aiResponsePane = new JEditorPane();
        aiResponsePane.setContentType("text/html");
        aiResponsePane.setEditable(false);
        aiResponsePane.setText("<html><body style='font-family: sans-serif; color: gray;'>Waiting for AI analysis...</body></html>");

        // DZIELIMY EKRAN NA PÓŁ (Góra / Dół)
        JBSplitter splitter = new JBSplitter(true, 0.4f); // 40% ekranu na górę, 60% na odpowiedź
        splitter.setFirstComponent(topPanel);
        splitter.setSecondComponent(new JBScrollPane(aiResponsePane));

        mainPanel.add(splitter, BorderLayout.CENTER);

        // --- NASŁUCHIWANIE DANYCH ---
        SharedStateService sharedState = project.getService(SharedStateService.class);
        sharedState.setOnDataUpdatedCallback(() -> {
            errorTextArea.setText(sharedState.getErrorMessage());
            if (sharedState.getSourceCode() != null && !sharedState.getSourceCode().isEmpty()) {
                contextLabel.setText("Context: Source code attached ✅");
                contextLabel.setForeground(JBColor.GREEN);
            } else {
                contextLabel.setText("Context: No source code ❌");
                contextLabel.setForeground(JBColor.RED);
            }
            sendToAiButton.setEnabled(true);
        });

        // --- LOGIKA ANALIZY ---
        sendToAiButton.addActionListener(e -> {
            String errorMessage = sharedState.getErrorMessage();
            String sourceCode = sharedState.getSourceCode();
            String apiKey = AiExplainerSettingsState.getInstance().apiKey;
            String providerName = AiExplainerSettingsState.getInstance().aiProvider;

            if (apiKey == null || apiKey.trim().isEmpty()) {
                aiResponsePane.setText("<html><body style='color: red;'>API Key is missing! Please configure it in Settings.</body></html>");
                return;
            }

            sendToAiButton.setEnabled(false);
            aiResponsePane.setText("<html><body style='font-family: sans-serif;'><i>AI is thinking... ⏳</i></body></html>");

            AiProvider aiProvider = AiProviderFactory.getProvider(providerName);
            aiProvider.analyzeError(errorMessage, sourceCode, apiKey)
                    .thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                JsonObject aiAnswerJson = new Gson().fromJson(response, JsonObject.class);
                                String summaryMd = aiAnswerJson.get("errorSummary").getAsString();
                                String causeMd = aiAnswerJson.get("rootCause").getAsString();
                                String fixMd = aiAnswerJson.get("suggestedFix").getAsString();

                                // MAGIA MARKDOWN: Tworzymy tłumacza
                                Parser parser = Parser.builder().build();
                                HtmlRenderer renderer = HtmlRenderer.builder().build();

                                // Tłumaczymy wartości Markdown od AI na czysty HTML!
                                String summaryHtml = renderer.render(parser.parse(summaryMd));
                                String causeHtml = renderer.render(parser.parse(causeMd));
                                String fixHtml = renderer.render(parser.parse(fixMd));

                                // FORMATOWANIE GŁÓWNE
                                // Dodajemy trochę CSS, żeby bloki kodu (<code>) ładnie wyglądały na szaro
                                String htmlOutput = "<html><head><style>" +
                                        "code { background-color: rgba(128, 128, 128, 0.2); padding: 2px 4px; border-radius: 4px; font-family: monospace; }" +
                                        "pre { background-color: rgba(128, 128, 128, 0.1); padding: 8px; border-radius: 4px; border: 1px solid rgba(128, 128, 128, 0.2); }" +                                        "pre { background-color: #f5f5f5; padding: 8px; border-radius: 4px; }" +
                                        "</style></head><body style='font-family: sans-serif; padding: 10px;'>" +
                                        "<h3 style='color: #d9534f;'>🛑 SUMMARY</h3>" + summaryHtml +
                                        "<h3 style='color: #f0ad4e;'>🔍 CAUSE</h3>" + causeHtml +
                                        "<h3 style='color: #5cb85c;'>🛠️ FIX</h3>" + fixHtml +
                                        "</body></html>";

                                aiResponsePane.setText(htmlOutput);
                            } catch (Exception parseEx) {
                                aiResponsePane.setText("<html><body><pre>" + response + "</pre></body></html>");
                            }
                            sendToAiButton.setEnabled(true);
                        });
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(() -> {
                            aiResponsePane.setText("<html><body style='color: red;'><b>PLUGIN ERROR:</b><br>" + ex.getMessage() + "</body></html>");
                            sendToAiButton.setEnabled(true);
                        });
                        return null;
                    });
        });

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}