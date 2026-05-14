package com.github.juliarzymowska.plugin.api;

/**
 * Utility class that stores system prompts used to instruct the AI models.
 * <p>
 * It centralizes the prompt engineering logic, making it easier to tweak the LLM's persona,
 * instructions, and expected output format without modifying the core business logic.
 * Instantiation is prevented as this class only holds static constants.
 */
public final class PromptTemplates {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PromptTemplates() {
    }

    /**
     * The system persona prompt used specifically for models that support
     * system/developer roles (like OpenAI). It defines the overarching behavior.
     */
    public static final String SYSTEM_ROLE_PROMPT = "You are an expert Java developer and debugging assistant.";

    /**
     * The primary prompt template used to request error analysis from the AI provider.
     * <p>
     * It instructs the AI to act as a Senior Software Engineer, defines the exact task
     * (identify the crash, explain the root cause, provide a fix), and enforces a strict
     * JSON output structure where the values contain rich Markdown formatting.
     * <p>
     * Designed to be used with {@link String#format(String, Object...)}, where:
     * <ul>
     *   <li>The first {@code %s} is replaced by the raw console error message.</li>
     *   <li>The second {@code %s} is replaced by the contextual source code.</li>
     * </ul>
     */
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