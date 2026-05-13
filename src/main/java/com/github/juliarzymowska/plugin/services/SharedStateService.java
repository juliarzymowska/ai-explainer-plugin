package com.github.juliarzymowska.plugin.services;

import com.intellij.openapi.components.Service;

/**
 * A project-level service acting as a central state manager and a communication bridge
 * between IDE actions (like context menu clicks) and the plugin's Tool Window UI.
 * <p>
 * It implements a simple Observer pattern using a callback, allowing the UI to reactively
 * update whenever new error data is injected by an action.
 */
@Service(Service.Level.PROJECT)
public final class SharedStateService {

    private String errorMessage = "";
    private String sourceCode = "";
    private Runnable onDataUpdatedCallback;

    /**
     * Updates the shared state with new error context and immediately notifies
     * any registered listeners (e.g., the Tool Window) that new data is available.
     *
     * @param errorMessage The raw error message or stack trace selected by the user.
     * @param sourceCode   The contextual source code extracted from the user's project.
     */
    public void setState(String errorMessage, String sourceCode) {
        this.errorMessage = errorMessage;
        this.sourceCode = sourceCode;

        if (onDataUpdatedCallback != null) {
            onDataUpdatedCallback.run();
        }
    }

    /**
     * Retrieves the currently stored error message.
     *
     * @return The error message string, or an empty string if no error has been set yet.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Retrieves the currently stored source code context.
     *
     * @return The source code string, or an empty string if no code context is available.
     */
    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * Registers a callback to be executed whenever the state is updated via {@link #setState(String, String)}.
     * This is typically used by the UI components to listen for background data changes.
     *
     * @param callback The {@link Runnable} to execute upon data update.
     */
    public void setOnDataUpdatedCallback(Runnable callback) {
        this.onDataUpdatedCallback = callback;
    }
}