package com.github.juliarzymowska.plugin.api;

public final class PromptTemplates {

    private PromptTemplates() {
    }

    public static final String ANALYZE_ERROR_PROMPT = """
                        You are a Senior Software Engineer helping a colleague debug their application.
                        Analyze the following console error, along with the relevant source code.
            
                        CONSOLE ERROR:
                        %s
            
                        SOURCE CODE:
                        %s
            
                        TASK:
                        1. Identify the exact line or component causing the crash.
                        2. Explain the root cause in simple terms.
                        3. Provide a concrete, actionable fix.
            
                        IMPORTANT FORMATTING RULES:
                        - Output MUST be strict JSON.
                        - Inside the JSON string values, use rich Markdown formatting (e.g., **bolding**, `inline code`, or ```java code blocks
            ```) to make it highly readable.
            
                        OUTPUT FORMAT (JSON only):
                        {
                          "errorSummary": "One sentence summary with Markdown",
                          "rootCause": "Detailed explanation using Markdown lists and bold text",
                          "suggestedFix": "Step-by-step fix with Markdown code snippets"
                        }
            """;
}