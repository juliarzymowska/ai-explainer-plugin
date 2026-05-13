package com.github.juliarzymowska.plugin.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

public class ApiKeyManager {

    private static final String SUBSYSTEM = "AiExplainerPlugin";

    public static void saveKey(String providerName, String apiKey) {
        CredentialAttributes attributes = createAttributes(providerName);
        // Zgodnie z dokumentacją: używamy obiektu Credentials
        // Podajemy null jako username, ponieważ trzymamy sam klucz (hasło)
        Credentials credentials = new Credentials(null, apiKey);
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    public static String getKey(String providerName) {
        // Metoda getPassword() jest poprawna według dokumentacji dla pobierania samego hasła
        return PasswordSafe.getInstance().getPassword(createAttributes(providerName));
    }

    private static CredentialAttributes createAttributes(String providerName) {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName(SUBSYSTEM, providerName + "_API_Key")
        );
    }
}