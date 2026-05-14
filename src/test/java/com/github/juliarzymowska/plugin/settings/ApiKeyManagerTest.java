package com.github.juliarzymowska.plugin.settings;

import com.github.juliarzymowska.plugin.api.providers.AiProviderType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ApiKeyManagerTest extends BasePlatformTestCase {

    private final String PROVIDER_NAME = AiProviderType.OPENAI.getDisplayName();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Reset the key before each test
        ApiKeyManager.saveKey(PROVIDER_NAME, null);
    }

    public void testSaveAndRetrieveKey() {
        String testKey = "sk-123456789";

        ApiKeyManager.saveKey(PROVIDER_NAME, testKey);
        String retrieved = ApiKeyManager.getKey(PROVIDER_NAME);

        assertEquals("The retrieved key should match the saved key", testKey, retrieved);
    }

    public void testClearKey() {
        ApiKeyManager.saveKey(PROVIDER_NAME, "temp-key");
        ApiKeyManager.saveKey(PROVIDER_NAME, ""); // Clearing the key

        String retrieved = ApiKeyManager.getKey(PROVIDER_NAME);
        assertNull("Retrieving a cleared key should return null", retrieved);
    }
}