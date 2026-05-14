package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProvider;
import com.github.juliarzymowska.plugin.api.providers.AiProviderFactory;
import com.github.juliarzymowska.plugin.api.providers.AiProviderType;
import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.github.juliarzymowska.plugin.settings.ApiKeyManager;
import com.github.juliarzymowska.plugin.utils.HtmlResponseRenderer;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * Controller responsible for orchestrating the AI analysis business logic.
 * <p>
 * It acts as the mediator between the UI (AiToolWindowPanel) and the backend APIs,
 * ensuring that the UI class remains clean and focused solely on rendering.
 */
public class AiAnalysisController {

    private final Project project;
    private final AiToolWindowPanel view;

    /**
     * Holds the ongoing asynchronous API request, allowing the user to cancel it.
     */
    private CompletableFuture<String> currentAnalysisFuture;

    public AiAnalysisController(Project project, AiToolWindowPanel view) {
        this.project = project;
        this.view = view;
    }

    /**
     * Orchestrates the API request lifecycle.
     * Validates keys, locks the UI, and dispatches the background request.
     *
     * @param selectedProviderDisplayName The name of the selected AI provider from the UI.
     */
    public void startAnalysis(String selectedProviderDisplayName) {
        SharedStateService sharedState = project.getService(SharedStateService.class);
        AiProviderType type = AiProviderType.fromDisplayName(selectedProviderDisplayName);
        String apiKey = ApiKeyManager.getKey(selectedProviderDisplayName);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            view.showError("API Key for " + selectedProviderDisplayName + " is missing in Settings!");
            return;
        }

        view.setLoadingState();
        AiProvider aiProvider = AiProviderFactory.getProvider(type);

        currentAnalysisFuture = aiProvider.analyzeError(sharedState.getErrorMessage(), sharedState.getSourceCode(), apiKey);

        currentAnalysisFuture
                .thenAccept(response -> SwingUtilities.invokeLater(() -> {
                    String finalHtml = HtmlResponseRenderer.render(response);
                    view.showResponse(finalHtml);
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> handleException(ex));
                    return null;
                });
    }

    /**
     * Attempts to cancel the ongoing asynchronous API request.
     */
    public void cancelAnalysis() {
        if (currentAnalysisFuture != null && !currentAnalysisFuture.isDone()) {
            currentAnalysisFuture.cancel(true);
            view.showWarning("Analysis cancelled by user.");
        }
    }

    /**
     * Processes exceptions from the background thread and updates the UI accordingly.
     */
    private void handleException(Throwable ex) {
        if (ex instanceof CancellationException || ex.getCause() instanceof CancellationException) {
            view.showWarning("Analysis cancelled by user.");
        } else {
            view.showError("Plugin Error:<br>" + ex.getMessage());
        }
    }
}