package com.github.juliarzymowska.plugin.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class HtmlResponseRendererTest {

    @Test
    public void shouldRenderValidJsonWithMarkdownToHtml() {
        // Input matches the keys expected by HtmlResponseRenderer.render()
        String validJson = """
                {
                  "errorSummary": "This is a **bold** summary.",
                  "rootCause": "Null value in `main`.",
                  "suggestedFix": "Add an if-statement."
                }
                """;

        String htmlOutput = HtmlResponseRenderer.render(validJson);

        // Verify the structure and expected content
        assertFalse("Renderer unexpectedly triggered the fallback <pre> block!", htmlOutput.contains("<pre>"));
        assertTrue("Missing SUMMARY header", htmlOutput.contains("SUMMARY"));
        assertTrue("Missing CAUSE header", htmlOutput.contains("CAUSE"));
        assertTrue("Missing FIX header", htmlOutput.contains("FIX"));

        // Verify Markdown conversion
        assertTrue("Markdown failed to render bold text", htmlOutput.contains("<strong>bold</strong>"));
        assertTrue("Markdown failed to render inline code", htmlOutput.contains("<code>main</code>"));
    }

    @Test
    public void shouldTriggerFallbackForBrokenJson() {
        // Input: Plain text instead of valid JSON string
        String brokenJson = "I am an AI, I forgot to format this as JSON. My bad!";

        String htmlOutput = HtmlResponseRenderer.render(brokenJson);

        // Check if the fallback mechanism catches the parsing error
        assertTrue("Missing basic HTML structure", htmlOutput.startsWith("<html><body"));
        assertTrue("Fallback failed to preserve the raw AI response",
                htmlOutput.contains("<pre>I am an AI, I forgot to format this as JSON. My bad!</pre>"));
    }

    @Test
    public void shouldTriggerFallbackForMissingFields() {
        // If "rootCause" is missing, the current renderer logic will throw an NPE and fallback
        String incompleteJson = """
                {
                  "errorSummary": "Something is wrong."
                }
                """;

        String htmlOutput = HtmlResponseRenderer.render(incompleteJson);

        assertTrue("Should fallback to <pre> when mandatory keys are missing", htmlOutput.contains("<pre>"));
    }
}