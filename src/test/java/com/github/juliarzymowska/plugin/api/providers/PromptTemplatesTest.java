package com.github.juliarzymowska.plugin.api.providers;

import com.github.juliarzymowska.plugin.api.PromptTemplates;
import org.junit.Test;
import static org.junit.Assert.*;

public class PromptTemplatesTest {

    @Test
    public void testPromptsAreNotEmpty() {
        assertNotNull("System prompt should not be null", PromptTemplates.SYSTEM_ROLE_PROMPT);
        assertFalse("System prompt should not be empty", PromptTemplates.SYSTEM_ROLE_PROMPT.trim().isEmpty());

        assertNotNull("Analyze prompt should not be null", PromptTemplates.ANALYZE_ERROR_PROMPT);
        assertFalse("Analyze prompt should not be empty", PromptTemplates.ANALYZE_ERROR_PROMPT.trim().isEmpty());
    }

    @Test
    public void testAnalyzeErrorPromptFormatting() {
        // Arrange: Prepare dummy data just like a user would highlight
        String dummyError = "NullPointerException at MyClass.java:42";
        String dummyCode = "String text = null;\ntext.length();";

        // Act: Attempt to format the prompt.
        // If the %s markers were accidentally deleted from the template, this might fail or omit data.
        String formattedPrompt = String.format(PromptTemplates.ANALYZE_ERROR_PROMPT, dummyError, dummyCode);

        // Assert: Verify the injected strings successfully made it into the final text
        assertTrue("Formatted prompt must contain the injected error message",
                formattedPrompt.contains(dummyError));

        assertTrue("Formatted prompt must contain the injected source code",
                formattedPrompt.contains(dummyCode));

        // Assert: Verify crucial JSON format instructions weren't accidentally overwritten
        assertTrue("Prompt must retain the strict JSON formatting instructions",
                formattedPrompt.contains("OUTPUT FORMAT (JSON only):"));
    }
}