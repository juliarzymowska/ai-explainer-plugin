package com.github.juliarzymowska.plugin.services;

import com.intellij.openapi.components.Service;

@Service(Service.Level.PROJECT)
public final class SharedStateService {

    private String errorMessage = "";
    private String sourceCode = "";
    private Runnable onDataUpdatedCallback;

    // Metoda używana przez naszą przyszłą akcję do wstrzykiwania danych
    public void setState(String errorMessage, String sourceCode) {
        this.errorMessage = errorMessage;
        this.sourceCode = sourceCode;
        // Powiadamiamy interfejs użytkownika, że nadeszły nowe dane
        if (onDataUpdatedCallback != null) {
            onDataUpdatedCallback.run();
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    // Pozwala Tool Window nasłuchiwać na nowe błędy
    public void setOnDataUpdatedCallback(Runnable callback) {
        this.onDataUpdatedCallback = callback;
    }
}