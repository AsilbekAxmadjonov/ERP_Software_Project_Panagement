package org.example.erp.modules.identity.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

/** Published before the caller is authenticated, so the event names its own actor. Never carries the token. */
public final class LoginSucceeded extends BaseIntegrationEvent {
    private final String username;

    public LoginSucceeded(String username) {
        super(username, null);
        this.username = username;
    }

    public String username() {
        return username;
    }

    @Override
    public String actor() {
        return username;
    }

    @Override
    public String eventName() {
        return "identity.login-succeeded";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("username", username);
    }
}
