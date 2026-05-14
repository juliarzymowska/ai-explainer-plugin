package com.github.juliarzymowska.plugin.services;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class SharedStateServiceTest extends BasePlatformTestCase {

    private SharedStateService stateService;

    /**
     * setUp() runs automatically before every single test.
     * It gives us a fresh environment.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp(); // Sets up the headless IntelliJ environment

        // 1. FIRST, fetch the service from the project
        stateService = getProject().getService(SharedStateService.class);

        // 2. THEN, clean up the state to prevent State Pollution
        stateService.setState("", "");
        stateService.setOnDataUpdatedCallback(null);
    }    public void testInitialStateIsEmpty() {
        // When a project is first opened, our service should have empty strings, not nulls
        assertEquals("", stateService.getErrorMessage());
        assertEquals("", stateService.getSourceCode());
    }

    public void testUpdatingStateStoresValuesCorrectly() {
        // Simulate a user selecting an error and code
        // CHANGED: updateData -> setState
        stateService.setState("NullPointerException at line 42", "String text = null;");

        // Verify the service held onto the data
        assertEquals("NullPointerException at line 42", stateService.getErrorMessage());
        assertEquals("String text = null;", stateService.getSourceCode());
    }

    public void testCallbackIsTriggeredOnUpdate() {
        // We use a boolean array as a clever way to change a variable inside a lambda
        final boolean[] callbackWasCalled = {false};

        // Register our listener
        stateService.setOnDataUpdatedCallback(() -> {
            callbackWasCalled[0] = true;
        });

        // Trigger an update
        // CHANGED: updateData -> setState
        stateService.setState("Error", "Code");

        // If the callback worked, the boolean should now be true
        assertTrue("The callback should have been triggered!", callbackWasCalled[0]);
    }
}