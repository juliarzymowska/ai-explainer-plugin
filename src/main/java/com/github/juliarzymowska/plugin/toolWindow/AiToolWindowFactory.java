package com.github.juliarzymowska.plugin.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * The entry point for creating the plugin's Tool Window (the side/bottom panel in the IDE).
 * <p>
 * This factory is registered in the plugin.xml and is instantiated by the IntelliJ
 * Platform when the user opens the "AI Explainer" tab for the first time in a project.
 */
public class AiToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        AiToolWindowPanel mainPanel = new AiToolWindowPanel(project);
        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}