package com.github.juliarzymowska.plugin.settings;

import com.github.juliarzymowska.plugin.api.providers.AiProviderType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public class AiExplainerConfigurableTest extends BasePlatformTestCase {

    private AiExplainerConfigurable configurable;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        configurable = new AiExplainerConfigurable();
        configurable.createComponent();

        ApiKeyManager.saveKey(AiProviderType.OPENAI.getDisplayName(), "");
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            if (configurable != null) {
                configurable.disposeUIResources();
            }
        } catch (Exception e) {
            addSuppressedException(e);
        } finally {
            super.tearDown();
        }
    }

    public void testIsModifiedIsFalseImmediatelyAfterReset() {
        configurable.reset();
        assertFalse("Configurable should not report as modified right after a reset", configurable.isModified());
    }

    public void testApplyUpdatesInternalCacheAndClearsModifiedFlag() {
        configurable.reset();
        configurable.apply();
        assertFalse("Configurable should not be modified after applying changes", configurable.isModified());
    }

    public void testResetPullsDataFromPersistentState() {
        List<String> validOpenAiModels = AiProviderType.OPENAI.getSupportedModels();
        String realModelToTest = validOpenAiModels.getLast();

        AiExplainerSettingsState.getInstance().selectedModels.put(AiProviderType.OPENAI.name(), realModelToTest);

        configurable.reset();
        configurable.apply();

        assertEquals(realModelToTest, AiExplainerSettingsState.getInstance().selectedModels.get(AiProviderType.OPENAI.name()));
    }
}