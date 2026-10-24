package org.example.erp.modules.identity.application.events;

import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/** Never carries the password or its hash. */
public final class UserCreated extends BaseIntegrationEvent {
    private final long userId;
    private final String username;
    private final String fullName;
    private final UserRole role;

    public UserCreated(long userId, String username, String fullName, UserRole role) {
        super(username, null);
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public long userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public UserRole role() {
        return role;
    }

    @Override
    public String eventName() {
        return "identity.user-created";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("fullName", fullName);
        payload.put("role", role.name());
        return payload;
    }
}
