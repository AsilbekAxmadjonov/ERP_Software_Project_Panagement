package org.example.erp.modules.identity.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

public final class UserDisabled extends BaseIntegrationEvent {
    private final long userId;
    private final String username;

    public UserDisabled(long userId, String username) {
        super(username, null);
        this.userId = userId;
        this.username = username;
    }

    public long userId() {
        return userId;
    }

    @Override
    public String eventName() {
        return "identity.user-disabled";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("userId", userId, "username", username);
    }
}
