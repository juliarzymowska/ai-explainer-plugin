package com.github.juliarzymowska.plugin.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class HtmlResponseRenderer {

    private static final Parser mdParser = Parser.builder().build();
    private static final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
    private static final Gson gson = new Gson();

    private static final String CSS_STYLE = "<style>" +
            "code { background-color: rgba(128, 128, 128, 0.2); padding: 2px 4px; border-radius: 4px; font-family: monospace; }" +
            "pre { background-color: rgba(128, 128, 128, 0.1); padding: 8px; border-radius: 4px; }" +
            "h3 { margin-bottom: 5px; }" +
            "</style>";

    public static String render(String rawJsonResponse) {
        try {
            JsonObject json = gson.fromJson(rawJsonResponse, JsonObject.class);

            String summaryHtml = htmlRenderer.render(mdParser.parse(json.get("errorSummary").getAsString()));
            String causeHtml = htmlRenderer.render(mdParser.parse(json.get("rootCause").getAsString()));
            String fixHtml = htmlRenderer.render(mdParser.parse(json.get("suggestedFix").getAsString()));

            return "<html><head>" + CSS_STYLE + "</head><body style='font-family: sans-serif; padding: 10px;'>" +
                    "<h3 style='color: #d9534f;'>🛑 SUMMARY</h3>" + summaryHtml +
                    "<h3 style='color: #f0ad4e;'>🔍 CAUSE</h3>" + causeHtml +
                    "<h3 style='color: #5cb85c;'>🛠️ FIX</h3>" + fixHtml +
                    "</body></html>";

        } catch (Exception e) {
            return "<html><body style='padding: 10px;'><pre>" + rawJsonResponse + "</pre></body></html>";
        }
    }
}