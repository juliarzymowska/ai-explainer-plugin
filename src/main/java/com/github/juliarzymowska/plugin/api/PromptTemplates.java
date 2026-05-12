package com.github.juliarzymowska.plugin.api;

public final class PromptTemplates {

    private PromptTemplates() {} // Zapobiega tworzeniu instancji tej klasy

    /**
     * Professional prompt template structured similarly to the Python example.
     * We explicitly demand a JSON output so we can easily parse and format it in our UI.
     */
    public static final String ANALYZE_ERROR_PROMPT = """
            You are a Senior Software Engineer helping a colleague debug their application.
            Analyze the following console output or stack trace and provide a clear, structured solution.
            
            CONSOLE OUTPUT:
            %s
            
            TASK:
            1. Identify the exact line or component causing the crash.
            2. Explain the root cause in simple terms.
            3. Provide a concrete, actionable fix.
            
            OUTPUT FORMAT (JSON only, no markdown blocks, no other text):
            {
              "errorSummary": "One sentence summary of what went wrong",
              "rootCause": "Detailed explanation of why it happened",
              "suggestedFix": "Step-by-step instructions to resolve the issue"
            }
            """;
}