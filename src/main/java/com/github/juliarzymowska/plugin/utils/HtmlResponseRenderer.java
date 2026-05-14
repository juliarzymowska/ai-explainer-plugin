package com.github.juliarzymowska.plugin.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Utility class responsible for parsing the JSON response from the AI provider
 * and converting its Markdown content into a fully styled HTML document.
 * It ensures the response is properly formatted for display in the plugin's UI.
 */
public class HtmlResponseRenderer {

    /**
     * Markdown parser used to interpret AI-generated text.
     */
    private static final Parser mdParser = Parser.builder().build();

    /**
     * HTML renderer used to convert parsed Markdown nodes into HTML tags.
     */
    private static final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    /**
     * JSON parser for deserializing the raw AI response.
     */
    private static final Gson gson = new Gson();

    /**
     * Embedded CSS styles for formatting code blocks and headers in the UI.
     * Modern properties like 'rgba' or 'border-radius' have been removed
     * to prevent NullPointerExceptions during theme switching.
     */
    private static final String CSS_STYLE = "<style>" +
            "code { padding: 2px; font-family: monospace; }" +
            "pre { padding: 8px; }" +
            "h3 { margin-bottom: 5px; }" +
            "</style>";
    /**
     * Parses the raw JSON response, extracts the specific error analysis fields,
     * converts their Markdown content to HTML, and wraps them in a styled HTML template.
     * <p>
     * If the provided string is not valid JSON (e.g., plain text fallback from the LLM),
     * it gracefully catches the parsing exception and wraps the raw text in a {@code <pre>} block.
     *
     * @param rawJsonResponse The raw, JSON-formatted string returned by the AI provider.
     * @return A complete HTML string ready to be rendered in a JEditorPane.
     */
    public static String render(String rawJsonResponse) {
        try {
            JsonObject json = gson.fromJson(rawJsonResponse, JsonObject.class);

            // Extract and convert Markdown fields to HTML
            String summaryHtml = htmlRenderer.render(mdParser.parse(json.get("errorSummary").getAsString()));
            String causeHtml = htmlRenderer.render(mdParser.parse(json.get("rootCause").getAsString()));
            String fixHtml = htmlRenderer.render(mdParser.parse(json.get("suggestedFix").getAsString()));

            // Assemble the final HTML document with injected CSS
            return "<html><head>" + CSS_STYLE + "</head><body style='font-family: sans-serif; padding: 10px;'>" +
                    "<h3 style='color: #d9534f;'>SUMMARY</h3>" + summaryHtml +
                    "<h3 style='color: #f0ad4e;'>CAUSE</h3>" + causeHtml +
                    "<h3 style='color: #5cb85c;'>FIX</h3>" + fixHtml +
                    "</body></html>";

        } catch (Exception e) {
            // Fallback mechanism for broken JSON or unexpected formats
            System.err.println("CRITICAL RENDERER ERROR: " + e.getMessage());
            return "<html><body style='padding: 10px;'><pre>" + rawJsonResponse + "</pre></body></html>";
        }
    }
}