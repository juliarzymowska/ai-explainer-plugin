package com.github.juliarzymowska.plugin.api;

public final class PromptTemplates {

    private PromptTemplates() {}

    public static final String ANALYZE_ERROR_PROMPT = """
            You are a Senior Software Engineer helping a colleague debug their application.
            Analyze the following console error, along with the relevant source code, and provide a clear, structured solution.
            
            CONSOLE ERROR / STACK TRACE:
            %s
            
            RELEVANT SOURCE CODE (if found):
            %s
            
            TASK:
            1. Identify the exact line or component causing the crash based on the provided code.
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