package org.example.erp.modules.identity.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

public final class UserEnabled extends BaseIntegrationEvent {
    private final long userId;
    private final String username;

    public UserEnabled(long userId, String username) {
        super(username, null);
        this.userId = userId;
        this.username = username;
    }

    public long userId() {
        return userId;
    }

    @Override
    public String eventName() {
        return "identity.user-enabled";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("userId", userId, "username", username);
    }
}
