package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProvider;
import com.github.juliarzymowska.plugin.api.providers.AiProviderFactory;
import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.github.juliarzymowska.plugin.settings.AiExplainerSettingsState;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;

public class AiToolWindowPanel extends JPanel {

    private final Project project;
    private final JTextArea errorTextArea;
    private final JEditorPane aiResponsePane;
    private final JButton sendToAiButton;
    private final JLabel contextLabel;
    private final ComboBox<String> providerSelector;

    private final Parser mdParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    public AiToolWindowPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());

        // --- 1. TOP PANEL: Selection and Controls ---
        errorTextArea = new JTextArea("Select an error -> Right Click -> 'AI Explainer: Analyze Error'");
        errorTextArea.setEditable(false);
        errorTextArea.setLineWrap(true);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Provider Selector (Quick Access)
        providerSelector = new ComboBox<>(new String[]{"Gemini", "OpenAI"});
        providerSelector.setSelectedItem(AiExplainerSettingsState.getInstance().activeProvider);

        sendToAiButton = new JButton("Analyze with AI");
        sendToAiButton.setEnabled(false);

        contextLabel = new JLabel("Context: None");
        contextLabel.setForeground(Color.GRAY);

        controlsPanel.add(new JLabel("Provider:"));
        controlsPanel.add(providerSelector);
        controlsPanel.add(sendToAiButton);
        controlsPanel.add(contextLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(new JBScrollPane(errorTextArea), BorderLayout.CENTER);
        topContainer.add(controlsPanel, BorderLayout.SOUTH);

        // --- 2. BOTTOM PANEL: AI Response ---
        aiResponsePane = new JEditorPane();
        aiResponsePane.setContentType("text/html");
        aiResponsePane.setEditable(false);
        aiResponsePane.setText("<html><body style='font-family: sans-serif; color: gray; padding: 10px;'>Waiting for analysis...</body></html>");

        // --- 3. LAYOUT: Splitter ---
        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(topContainer);
        splitter.setSecondComponent(new JBScrollPane(aiResponsePane));

        add(splitter, BorderLayout.CENTER);

        setupListeners();
    }

    private void setupListeners() {
        // Listen to background data updates
        SharedStateService sharedState = project.getService(SharedStateService.class);
        sharedState.setOnDataUpdatedCallback(() -> {
            errorTextArea.setText(sharedState.getErrorMessage());
            if (sharedState.getSourceCode() != null && !sharedState.getSourceCode().isEmpty()) {
                contextLabel.setText("Context: Source code attached ✅");
                contextLabel.setForeground(new Color(0, 150, 0));
            } else {
                contextLabel.setText("Context: No source code ❌");
                contextLabel.setForeground(Color.RED);
            }
            sendToAiButton.setEnabled(true);
        });

        // Analyze button logic
        sendToAiButton.addActionListener(e -> performAnalysis());
    }

    private void performAnalysis() {
        SharedStateService sharedState = project.getService(SharedStateService.class);
        AiExplainerSettingsState settings = AiExplainerSettingsState.getInstance();

        String selectedProvider = (String) providerSelector.getSelectedItem();
        String apiKey = "Gemini".equals(selectedProvider) ? settings.geminiApiKey : settings.openAiApiKey;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            updateAiResponse("<html><body style='color: red; padding: 10px;'><b>Error:</b> API Key for " + selectedProvider + " is missing in Settings!</body></html>");
            return;
        }

        sendToAiButton.setEnabled(false);
        updateAiResponse("<html><body style='font-family: sans-serif; padding: 10px;'><i>AI is thinking... ⏳</i></body></html>");

        AiProvider aiProvider = AiProviderFactory.getProvider(selectedProvider);
        aiProvider.analyzeError(sharedState.getErrorMessage(), sharedState.getSourceCode(), apiKey)
                .thenAccept(response -> SwingUtilities.invokeLater(() -> handleAiResponse(response, sharedState.getErrorMessage())))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        updateAiResponse("<html><body style='color: red; padding: 10px;'><b>Plugin Error:</b><br>" + ex.getMessage() + "</body></html>");
                        sendToAiButton.setEnabled(true);
                    });
                    return null;
                });
    }

    private void handleAiResponse(String response, String originalError) {
        try {
            JsonObject json = new Gson().fromJson(response, JsonObject.class);
            String summaryHtml = htmlRenderer.render(mdParser.parse(json.get("errorSummary").getAsString()));
            String causeHtml = htmlRenderer.render(mdParser.parse(json.get("rootCause").getAsString()));
            String fixHtml = htmlRenderer.render(mdParser.parse(json.get("suggestedFix").getAsString()));

            String finalHtml = "<html><head><style>" +
                    "code { background-color: rgba(128, 128, 128, 0.2); padding: 2px 4px; border-radius: 4px; font-family: monospace; }" +
                    "pre { background-color: rgba(128, 128, 128, 0.1); padding: 8px; border-radius: 4px; }" +
                    "h3 { margin-bottom: 5px; }" +
                    "</style></head><body style='font-family: sans-serif; padding: 10px;'>" +
                    "<h3 style='color: #d9534f;'>🛑 SUMMARY</h3>" + summaryHtml +
                    "<h3 style='color: #f0ad4e;'>🔍 CAUSE</h3>" + causeHtml +
                    "<h3 style='color: #5cb85c;'>🛠️ FIX</h3>" + fixHtml +
                    "</body></html>";

            updateAiResponse(finalHtml);
        } catch (Exception e) {
            updateAiResponse("<html><body style='padding: 10px;'><pre>" + response + "</pre></body></html>");
        }
        sendToAiButton.setEnabled(true);
    }

    private void updateAiResponse(String html) {
        aiResponsePane.setText(html);
    }
}