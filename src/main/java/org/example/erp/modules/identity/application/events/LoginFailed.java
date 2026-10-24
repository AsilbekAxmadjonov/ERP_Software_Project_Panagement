package org.example.erp.modules.identity.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

/**
 * A rejected login attempt for the username that was typed, which may not exist. The attempted password is never
 * included; reason is "unknown-user", "inactive-user" or "wrong-password".
 */
public final class LoginFailed extends BaseIntegrationEvent {
    public static final String UNKNOWN_USER = "unknown-user";
    public static final String INACTIVE_USER = "inactive-user";
    public static final String WRONG_PASSWORD = "wrong-password";

    private final String username;
    private final String reason;

    public LoginFailed(String username, String reason) {
        super(username, null);
        this.username = username;
        this.reason = reason;
    }

    public String username() {
        return username;
    }

    public String reason() {
        return reason;
    }

    @Override
    public String actor() {
        return username;
    }

    @Override
    public String eventName() {
        return "identity.login-failed";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("username", username, "reason", reason);
    }
}
