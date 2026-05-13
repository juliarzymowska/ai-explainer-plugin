package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProvider;
import com.github.juliarzymowska.plugin.api.providers.AiProviderFactory;
import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.github.juliarzymowska.plugin.settings.ApiKeyManager;
import com.github.juliarzymowska.plugin.utils.HtmlResponseRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;

/**
 * The main graphical user interface for the AI Explainer tool window.
 * <p>
 * This panel is responsible for displaying the captured error context, allowing the user
 * to select an AI provider, dispatching the asynchronous API request, and safely rendering
 * the resulting HTML analysis back onto the main UI thread.
 */
public class AiToolWindowPanel extends JPanel {

    private final Project project;
    private final JTextArea errorTextArea;
    private final JEditorPane aiResponsePane;
    private final JButton sendToAiButton;
    private final JButton stopButton;
    private final JLabel contextLabel;
    private final ComboBox<String> providerSelector;

    /**
     * Holds the ongoing asynchronous API request, allowing the user to cancel it
     * before it completes.
     */
    private CompletableFuture<String> currentAnalysisFuture;

    public AiToolWindowPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());

        // --- 1. TOP PANEL: Selection and Controls ---
        errorTextArea = new JTextArea("Select an error -> Right Click -> 'AI Explainer: Analyze Error'");
        errorTextArea.setEditable(false);
        errorTextArea.setLineWrap(true);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        providerSelector = new ComboBox<>(new String[]{"Gemini", "OpenAI"});
        providerSelector.setSelectedIndex(0);

        sendToAiButton = new JButton("Analyze with AI");
        sendToAiButton.setEnabled(false);

        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        contextLabel = new JLabel("Context: None");
        contextLabel.setForeground(JBColor.GRAY);

        controlsPanel.add(new JLabel("Provider:"));
        controlsPanel.add(providerSelector);
        controlsPanel.add(sendToAiButton);
        controlsPanel.add(stopButton);
        controlsPanel.add(contextLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(new JBScrollPane(errorTextArea), BorderLayout.CENTER);
        topContainer.add(controlsPanel, BorderLayout.SOUTH);

        // --- 2. BOTTOM PANEL: AI Response ---
        aiResponsePane = new JEditorPane();
        aiResponsePane.setContentType("text/html");
        aiResponsePane.setEditable(false);

        // UX Trick: Hide the blinking cursor while still allowing text selection and copying
        aiResponsePane.putClientProperty("caretWidth", 0);

        aiResponsePane.setText("<html><body style='font-family: sans-serif; color: gray; padding: 10px;'>Waiting for analysis...</body></html>");

        // --- 3. LAYOUT: Splitter ---
        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(topContainer);
        splitter.setSecondComponent(new JBScrollPane(aiResponsePane));

        add(splitter, BorderLayout.CENTER);

        setupListeners();
    }

    /**
     * Initializes all event listeners for the UI components and subscribes to the
     * {@link SharedStateService} to reactively update the UI when new errors are selected.
     */
    private void setupListeners() {
        SharedStateService sharedState = project.getService(SharedStateService.class);

        sharedState.setOnDataUpdatedCallback(() -> {
            errorTextArea.setText(sharedState.getErrorMessage());

            if (sharedState.getSourceCode() != null && !sharedState.getSourceCode().isEmpty()) {
                contextLabel.setText("Context: Source code attached \u2705");
                contextLabel.setForeground(new Color(0, 150, 0));
            } else {
                contextLabel.setText("Context: No source code \u274C");
                contextLabel.setForeground(Color.RED);
            }
            sendToAiButton.setEnabled(true);
        });

        sendToAiButton.addActionListener(e -> performAnalysis());
        stopButton.addActionListener(e -> cancelAnalysis());
    }

    /**
     * Orchestrates the API request lifecycle.
     * <p>
     * It validates the API key, locks the UI buttons, dispatches the network request on a
     * background thread, and ensures that all UI updates (both success and error states)
     * are pushed back to the Event Dispatch Thread (EDT) via {@link SwingUtilities#invokeLater(Runnable)}.
     */
    private void performAnalysis() {
        SharedStateService sharedState = project.getService(SharedStateService.class);
        String selectedProvider = (String) providerSelector.getSelectedItem();
        String apiKey = ApiKeyManager.getKey(selectedProvider);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            updateAiResponse("<html><body style='color: red; padding: 10px;'><b>Error:</b> API Key for " + selectedProvider + " is missing in Settings!</body></html>");
            return;
        }

        // Lock UI during analysis
        sendToAiButton.setEnabled(false);
        stopButton.setEnabled(true);
        updateAiResponse("<html><body style='font-family: sans-serif; padding: 10px;'><i>AI is thinking... \u23F3</i></body></html>");

        AiProvider aiProvider = AiProviderFactory.getProvider(selectedProvider);

        currentAnalysisFuture = aiProvider.analyzeError(sharedState.getErrorMessage(), sharedState.getSourceCode(), apiKey);

        currentAnalysisFuture
                .thenAccept(response -> SwingUtilities.invokeLater(() -> {
                    handleAiResponse(response, sharedState.getErrorMessage());
                    stopButton.setEnabled(false);
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        // Check if the exception was triggered by the user clicking "Stop"
                        if (ex instanceof CancellationException || ex.getCause() instanceof CancellationException) {
                            updateAiResponse("<html><body style='color: orange; padding: 10px;'><b>⚠️ Analysis cancelled by user.</b></body></html>");
                        } else {
                            updateAiResponse("<html><body style='color: red; padding: 10px;'><b>Plugin Error:</b><br>" + ex.getMessage() + "</body></html>");
                        }
                        sendToAiButton.setEnabled(true);
                        stopButton.setEnabled(false);
                    });
                    return null;
                });
    }

    /**
     * Attempts to cancel the ongoing asynchronous API request if it hasn't completed yet.
     */
    private void cancelAnalysis() {
        if (currentAnalysisFuture != null && !currentAnalysisFuture.isDone()) {
            currentAnalysisFuture.cancel(true);
        }
    }

    /**
     * Delegates the raw JSON response to the renderer and updates the UI.
     *
     * @param response      The raw JSON string returned by the AI provider.
     * @param originalError The original error message (reserved for future context usage).
     */
    private void handleAiResponse(String response, String originalError) {
        String finalHtml = HtmlResponseRenderer.render(response);
        updateAiResponse(finalHtml);
        sendToAiButton.setEnabled(true);
    }

    /**
     * Injects the final HTML string into the JEditorPane.
     *
     * @param html The fully formatted HTML string.
     */
    private void updateAiResponse(String html) {
        aiResponsePane.setText(html);
    }
}