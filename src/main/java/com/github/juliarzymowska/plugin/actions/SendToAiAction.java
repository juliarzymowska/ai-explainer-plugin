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

/**
 * Represents the main action triggered by the user from the IDE editor (e.g., via the context menu).
 * This action captures the currently selected error text, attempts to extract the associated
 * source code file mentioned in that text, updates the plugin's shared state, and opens
 * the "AI Explainer" tool window to initiate the analysis.
 */
public class SendToAiAction extends AnAction {

    /**
     * Executed when the user invokes the action in the IDE.
     * <p>
     * The method performs the following steps:
     * <ol>
     *   <li>Verifies that a project and an editor are currently active.</li>
     *   <li>Retrieves the text explicitly selected by the user.</li>
     *   <li>Attempts to find and read the source code of the file mentioned in the selection.</li>
     *   <li>Passes the extracted error message and source code to the {@link SharedStateService}.</li>
     *   <li>Opens and brings focus to the "AI Explainer" tool window.</li>
     * </ol>
     *
     * @param e The event containing the context of the action invocation (project, editor, etc.).
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) return;

        String errorMessage = editor.getSelectionModel().getSelectedText();

        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return;
        }

        String sourceCode = extractSourceCode(project, errorMessage);

        SharedStateService sharedState = project.getService(SharedStateService.class);
        sharedState.setState(errorMessage, sourceCode);

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("AI Explainer");
        if (toolWindow != null) {
            toolWindow.show(null);
        }
    }

    /**
     * Analyzes the provided error message using a Regular Expression to find a Java file reference
     * (e.g., "MyClass.java"). If found, it searches the current project's scope for a file with
     * that exact name and extracts its full source code.
     *
     * @param project      The current IntelliJ project instance, used to scope the virtual file search.
     * @param errorMessage The selected error text that might contain a file name.
     * @return A formatted string containing the file name and its source code,
     * or an empty string if no file reference was found or the file could not be read.
     */
    private String extractSourceCode(Project project, String errorMessage) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_]+\\.java)");
        Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            String fileName = matcher.group(1);

            Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                    fileName,
                    GlobalSearchScope.projectScope(project)
            );

            if (!files.isEmpty()) {
                VirtualFile file = files.iterator().next();

                Document document = FileDocumentManager.getInstance().getDocument(file);
                if (document != null) {
                    return "--- File: " + fileName + " ---\n" + document.getText();
                }
            }
        }

        return "";
    }
}