package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProviderType;
import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.openapi.Disposable;

import javax.swing.*;
import java.awt.*;

/**
 * The main graphical user interface for the AI Explainer tool window.
 * <p>
 * This class is strictly a "View" component. All business logic, thread management,
 * and API calls are delegated to the {@link AiAnalysisController}.
 */
public class AiToolWindowPanel extends JPanel implements Disposable {

    private final Project project;
    private final JTextArea errorTextArea;
    private final JEditorPane aiResponsePane;
    private final JButton sendToAiButton;
    private final JButton stopButton;
    private final JLabel contextLabel;
    private final ComboBox<String> providerSelector;
    private String lastHtmlResponse;

    // The controller handling the actual logic
    private final AiAnalysisController controller;

    public AiToolWindowPanel(Project project) {
        this.project = project;
        this.controller = new AiAnalysisController(project, this);

        setLayout(new BorderLayout());

        // --- 1. TOP PANEL: Selection and Controls ---
        errorTextArea = new JTextArea("Select an error -> Right Click -> 'AI Explainer: Analyze Error'");
        errorTextArea.setEditable(false);
        errorTextArea.setLineWrap(true);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        String[] providerNames = java.util.Arrays.stream(AiProviderType.values())
                .map(AiProviderType::getDisplayName)
                .toArray(String[]::new);
        providerSelector = new ComboBox<>(providerNames);
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
        aiResponsePane.setEditorKit(HTMLEditorKitBuilder.simple());
        aiResponsePane.setEditable(false);
        // UX Trick: Hide the blinking cursor while still allowing text selection and copying
        aiResponsePane.putClientProperty("caretWidth", 0);

        updateAiResponse("<html><body style='font-family: sans-serif; color: gray; padding: 10px;'>Waiting for analysis...</body></html>");

        // --- 3. LAYOUT: Splitter ---
        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(topContainer);
        splitter.setSecondComponent(new JBScrollPane(aiResponsePane));

        add(splitter, BorderLayout.CENTER);

        setupListeners();
    }

    /**
     * Initializes UI event listeners and delegates actions to the Controller.
     */
    private void setupListeners() {
        SharedStateService sharedState = project.getService(SharedStateService.class);

        // Listen for new errors selected by the user
        sharedState.setOnDataUpdatedCallback(() -> {
            errorTextArea.setText(sharedState.getErrorMessage());

            if (sharedState.getSourceCode() != null && !sharedState.getSourceCode().isEmpty()) {
                contextLabel.setText("Context: Source code attached \u2705");
                contextLabel.setForeground(JBColor.GREEN);
            } else {
                contextLabel.setText("Context: No source code \u274C");
                contextLabel.setForeground(JBColor.RED);
            }
            sendToAiButton.setEnabled(true);
        });

        // Delegate button clicks to the controller
        sendToAiButton.addActionListener(e -> controller.startAnalysis((String) providerSelector.getSelectedItem()));
        stopButton.addActionListener(e -> controller.cancelAnalysis());

        // Listen for IDE theme changes to re-render HTML colors
        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(
                LafManagerListener.TOPIC,
                new LafManagerListener() {
                    @Override
                    public void lookAndFeelChanged(@org.jetbrains.annotations.NotNull LafManager source) {
                        SwingUtilities.invokeLater(() -> {
                            if (lastHtmlResponse != null) {
                                aiResponsePane.setText(lastHtmlResponse);
                            }
                        });
                    }
                }
        );
    }

    // --- PUBLIC API FOR THE CONTROLLER ---

    /**
     * Locks the UI and shows a loading state.
     */
    public void setLoadingState() {
        sendToAiButton.setEnabled(false);
        stopButton.setEnabled(true);
        updateAiResponse("<html><body style='font-family: sans-serif; padding: 10px;'><i>AI is thinking... \u23F3</i></body></html>");
    }

    /**
     * Unlocks the UI and displays the successful HTML response.
     */
    public void showResponse(String html) {
        updateAiResponse(html);
        sendToAiButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    /**
     * Displays a red error message.
     */
    public void showError(String message) {
        String errorColor = com.intellij.ui.ColorUtil.toHtmlColor(JBColor.RED);
        updateAiResponse("<html><body style='color: " + errorColor + "; padding: 10px;'><b>Error:</b> " + message + "</body></html>");
        sendToAiButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    /**
     * Displays an orange warning message.
     */
    public void showWarning(String message) {
        String warningColor = ColorUtil.toHtmlColor(JBColor.ORANGE);
        updateAiResponse("<html><body style='color: " + warningColor + "; padding: 10px;'><b>⚠️ " + message + "</b></body></html>");
        sendToAiButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    /**
     * Internal helper to safely update the JEditorPane text and cache it for theme changes.
     */
    private void updateAiResponse(String html) {
        this.lastHtmlResponse = html;
        aiResponsePane.setText(html);
    }

    @Override
    public void dispose() {
        // Left empty intentionally. Required for Disposer tree registration.
    }
}