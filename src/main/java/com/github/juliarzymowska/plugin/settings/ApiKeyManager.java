package com.github.juliarzymowska.plugin.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

/**
 * Utility class responsible for securely managing API keys using the IntelliJ Platform's Credential Store API (PasswordSafe).
 * It ensures that sensitive data is not stored in plain text configuration files (XML),
 * but rather delegated to the operating system's native keychain or credential manager.
 */
public class ApiKeyManager {

    /**
     * The subsystem identifier used to uniquely group this plugin's credentials in the system password manager.
     */
    private static final String SUBSYSTEM = "AiExplainerPlugin";

    /**
     * Securely saves the given API key for a specific AI provider to the system's credential store.
     * <p>
     * Note: The username field in the {@link Credentials} object is intentionally set to {@code null}
     * because we are only storing a single secret token (the API key), not a username-password pair.
     *
     * @param providerName The name of the AI provider (e.g., "OpenAI", "Gemini"). Used to generate a unique storage key.
     * @param apiKey       The plain text API key to be encrypted and stored.
     */
    public static void saveKey(String providerName, String apiKey) {
        CredentialAttributes attributes = createAttributes(providerName);
        Credentials credentials = new Credentials(null, apiKey);
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    /**
     * Retrieves the stored API key for the specified AI provider from the system's credential store.
     *
     * @param providerName The name of the AI provider (e.g., "OpenAI", "Gemini").
     * @return The stored API key as a string, or {@code null} if no key is found for the given provider.
     */
    public static String getKey(String providerName) {
        return PasswordSafe.getInstance().getPassword(createAttributes(providerName));
    }

    /**
     * Generates the unique credential attributes required by PasswordSafe to identify a specific secret.
     * It creates a standardized service name format, for example: {@code AiExplainerPlugin - OpenAI_API_Key}.
     *
     * @param providerName The name of the AI provider.
     * @return The {@link CredentialAttributes} object used as a unique identifier in the credential store.
     */
    private static CredentialAttributes createAttributes(String providerName) {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName(SUBSYSTEM, providerName + "_API_Key")
        );
    }
}