package com.github.juliarzymowska.plugin.actions;

import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendToAiAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        // Zabezpieczenie: jeśli nie ma projektu lub edytora, przerywamy
        if (project == null || editor == null) return;

        // 1. Zczytujemy to, co użytkownik zaznaczył myszką
        String errorMessage = editor.getSelectionModel().getSelectedText();

        // Jeśli użytkownik kliknął prawym bez zaznaczania tekstu, nic nie robimy
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return;
        }

        // 2. Szukamy nazwy pliku i czytamy jego kod z projektu!
        String sourceCode = extractSourceCode(project, errorMessage);

        // 3. Wrzucamy dane do naszego serwisu ("Kuriera")
        SharedStateService sharedState = project.getService(SharedStateService.class);
        sharedState.setState(errorMessage, sourceCode);

        // 4. Otwieramy/Pokazujemy naszą zakładkę na dole ekranu
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("AI Explainer");
        if (toolWindow != null) {
            toolWindow.show(null);
        }
    }

    /**
     * Analizuje błąd używając Regex i szuka pliku w projekcie przez API JetBrains.
     */
    private String extractSourceCode(Project project, String errorMessage) {
        // Regex szukający wzorca typu: NazwaKlasy.java (np. World.java)
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_]+\\.java)");
        Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            String fileName = matcher.group(1); // Mamy nazwę pliku, np. "World.java"

            // Magia JetBrains: Przeszukujemy cały projekt w poszukiwaniu pliku o tej nazwie
            Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                    fileName,
                    GlobalSearchScope.projectScope(project)
            );

            // Jeśli znaleźliśmy taki plik w projekcie...
            if (!files.isEmpty()) {
                VirtualFile file = files.iterator().next(); // Bierzemy pierwszy pasujący plik

                // Konwertujemy wirtualny plik IDE na zwykły dokument tekstowy
                Document document = FileDocumentManager.getInstance().getDocument(file);
                if (document != null) {
                    return "--- File: " + fileName + " ---\n" + document.getText();
                }
            }
        }

        // Zwracamy puste, jeśli nie znaleziono pliku lub wzorca w błędzie
        return "";
    }
}