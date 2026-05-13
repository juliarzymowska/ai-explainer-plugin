package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProvider;
import com.github.juliarzymowska.plugin.api.providers.AiProviderFactory;
import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.github.juliarzymowska.plugin.settings.ApiKeyManager;
import com.github.juliarzymowska.plugin.utils.HtmlResponseRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

public class AiToolWindowPanel extends JPanel {

    private final Project project;
    private final JTextArea errorTextArea;
    private final JEditorPane aiResponsePane;
    private final JButton sendToAiButton;
    private final JLabel contextLabel;
    private final ComboBox<String> providerSelector;

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
        providerSelector.setSelectedIndex(0); // Ustawiamy Gemini jako domyślne przy starcie

        sendToAiButton = new JButton("Analyze with AI");
        sendToAiButton.setEnabled(false);

        contextLabel = new JLabel("Context: None");
        contextLabel.setForeground(Color.GRAY);

        controlsPanel.add(new JLabel("Provider:"));
        controlsPanel.add(providerSelector); // Odkomentowane - przycisk wróci do interfejsu!
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

        // Zamiast grzebać w starym State, pobieramy dostawcę z UI i strzelamy do bezpiecznego sejfu
        String selectedProvider = (String) providerSelector.getSelectedItem();
        String apiKey = ApiKeyManager.getKey(selectedProvider);

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
        String finalHtml = HtmlResponseRenderer.render(response);
        updateAiResponse(finalHtml);
        sendToAiButton.setEnabled(true);
    }

    private void updateAiResponse(String html) {
        aiResponsePane.setText(html);
    }
}