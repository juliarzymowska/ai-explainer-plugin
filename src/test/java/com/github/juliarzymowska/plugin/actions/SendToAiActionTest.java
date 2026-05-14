package com.github.juliarzymowska.plugin.actions;

import com.github.juliarzymowska.plugin.services.SharedStateService;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class SendToAiActionTest extends BasePlatformTestCase {

    private SharedStateService sharedState;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sharedState = getProject().getService(SharedStateService.class);

        // Clean up the state before each test to prevent State Pollution
        sharedState.setState("", "");
    }

    public void testActionDoesNothingWhenNoTextSelected() {
        // Arrange: Create a file and open it in the dummy editor, but don't select anything
        myFixture.configureByText("dummy.txt", "Just some random text with no selection.");

        // Act: Simulate the user clicking our action
        myFixture.testAction(new SendToAiAction());

        // Assert: The state should remain completely empty
        assertEquals("", sharedState.getErrorMessage());
        assertEquals("", sharedState.getSourceCode());
    }

    public void testActionCapturesErrorButNoSourceWhenNoJavaFileMentioned() {
        // Arrange: The <selection> tags automatically highlight the text in the headless IDE!
        myFixture.configureByText("dummy.txt", "Some text. <selection>NullPointerException in core logic</selection> more text.");

        // Act
        myFixture.testAction(new SendToAiAction());

        // Assert: It should capture the highlighted text, but the source code should be empty
        assertEquals("NullPointerException in core logic", sharedState.getErrorMessage());
        assertEquals("", sharedState.getSourceCode());
    }

    public void testActionExtractsSourceCodeWhenJavaFileMentioned() {
        // Arrange Step 1: Create a dummy Java file in our headless project
        String dummyJavaCode = "public class MyTargetClass {\n    public void crash() {}\n}";
        myFixture.addFileToProject("MyTargetClass.java", dummyJavaCode);

        // Arrange Step 2: Open a log file and select text that mentions our Java file
        myFixture.configureByText("error.log",
                "Exception in thread main: <selection>java.lang.NullPointerException at MyTargetClass.java:2</selection>");

        // Act: Execute the action
        myFixture.testAction(new SendToAiAction());

        // Assert: Verify it grabbed the error text AND successfully found and read the other file!
        assertEquals("java.lang.NullPointerException at MyTargetClass.java:2", sharedState.getErrorMessage());

        String extractedSource = sharedState.getSourceCode();
        assertTrue("Source code should contain the file header", extractedSource.contains("--- File: MyTargetClass.java ---"));
        assertTrue("Source code should contain the actual code", extractedSource.contains("public void crash() {}"));
    }
}