package com.github.juliarzymowska.plugin.toolWindow;

import com.github.juliarzymowska.plugin.api.providers.AiProviderType;
import com.github.juliarzymowska.plugin.settings.ApiKeyManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class AiAnalysisControllerTest extends BasePlatformTestCase {

    private static class FakeAiToolWindowPanel extends AiToolWindowPanel {
        public String displayedError = null;
        public boolean isLoadingStateSet = false;

        public FakeAiToolWindowPanel(Project project) {
            super(project);
        }

        @Override
        public void showError(String message) {
            this.displayedError = message;
        }

        @Override
        public void setLoadingState() {
            this.isLoadingStateSet = true;
        }
    }

    private FakeAiToolWindowPanel fakeView;
    private AiAnalysisController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        fakeView = new FakeAiToolWindowPanel(getProject());
        controller = new AiAnalysisController(getProject(), fakeView);

        ApiKeyManager.saveKey(AiProviderType.OPENAI.getDisplayName(), "");
    }

    public void testStartAnalysisShowsErrorWhenApiKeyIsMissing() {
        String providerName = AiProviderType.OPENAI.getDisplayName();

        controller.startAnalysis(providerName);

        assertNotNull("Controller should have called showError()", fakeView.displayedError);
        assertTrue("Error message should mention API Key", fakeView.displayedError.contains("API Key"));
    }

    public void testStartAnalysisSetsLoadingStateWhenApiKeyExists() {
        String providerName = AiProviderType.OPENAI.getDisplayName();

        ApiKeyManager.saveKey(providerName, "fake-test-key-123");

        controller.startAnalysis(providerName);

        assertTrue("Controller should have called setLoadingState()", fakeView.isLoadingStateSet);
    }
}