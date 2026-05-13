package com.github.juliarzymowska.plugin.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class HtmlResponseRendererTest {

    @Test
    public void shouldRenderValidJsonWithMarkdownToHtml() {
        // Input: Perfect JSON with Markdown from AI
        String validJson = """
                {
                  "errorSummary": "This is a **bold** summary.",
                  "rootCause": "Null value in `main`.",
                  "suggestedFix": "Add an if-statement."
                }
                """;

        String htmlOutput = HtmlResponseRenderer.render(validJson);

        // Assertions with clear English failure messages
        assertFalse("Renderer unexpectedly triggered the fallback <pre> block!", htmlOutput.contains("<pre>"));
        assertTrue("Missing SUMMARY header", htmlOutput.contains("SUMMARY"));
        assertTrue("Missing body tag", htmlOutput.contains("<body"));
        assertTrue("Markdown failed to render bold text", htmlOutput.contains("<strong>bold</strong>"));
        assertTrue("Markdown failed to render inline code", htmlOutput.contains("<code>main</code>"));
    }

    @Test
    public void shouldHandleMissingFieldsGracefully() {
        // Input: Missing "suggestedFix" field
        String incompleteJson = """
                {
                  "errorSummary": "Error here.",
                  "rootCause": "I don't know."
                }
                """;

        String htmlOutput = HtmlResponseRenderer.render(incompleteJson);

        // Ensure it gracefully falls back to raw text wrap without crashing
        assertTrue("Expected fallback <pre> block missing", htmlOutput.contains("<pre>"));
        assertTrue("Original text missing from fallback", htmlOutput.contains("Error here."));
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
}