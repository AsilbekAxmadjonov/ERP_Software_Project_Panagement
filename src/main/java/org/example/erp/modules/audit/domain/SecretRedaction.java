package org.example.erp.modules.audit.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The audit trail must never hold credentials. Events are not supposed to carry them, but the log records events from
 * every module, including ones written later, so any key that looks like a secret is dropped before storage.
 */
final class SecretRedaction {
    private static final Pattern SECRET = Pattern.compile("password|passwd|secret|token|credential|apikey|api_key|hash|pin$");

    private SecretRedaction() {}

    static boolean isSecret(String key) {
        return SECRET.matcher(key.toLowerCase(Locale.ROOT)).find();
    }

    static Map<String, Object> redact(Map<String, Object> payload) {
        Map<String, Object> safe = new LinkedHashMap<>();
        if (payload == null) return safe;
        payload.forEach((key, value) -> {
            // A flag such as passwordChanged says that something happened without revealing the secret itself.
            if (value instanceof Boolean || !isSecret(key)) safe.put(key, clean(value));
        });
        return safe;
    }

    @SuppressWarnings("unchecked")
    private static Object clean(Object value) {
        if (value instanceof Map<?, ?> map) return redact((Map<String, Object>) map);
        if (value instanceof List<?> list) return list.stream().map(SecretRedaction::clean).toList();
        return value;
    }
}
