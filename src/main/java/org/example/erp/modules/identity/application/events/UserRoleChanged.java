package org.example.erp.modules.identity.application.events;

import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

public final class UserRoleChanged extends BaseIntegrationEvent {
    private final long userId;
    private final String username;
    private final UserRole previousRole;
    private final UserRole role;

    public UserRoleChanged(long userId, String username, UserRole previousRole, UserRole role) {
        super(username, null);
        this.userId = userId;
        this.username = username;
        this.previousRole = previousRole;
        this.role = role;
    }

    public long userId() {
        return userId;
    }

    public UserRole previousRole() {
        return previousRole;
    }

    public UserRole role() {
        return role;
    }

    @Override
    public String eventName() {
        return "identity.user-role-changed";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("userId", userId, "username", username, "previousRole", previousRole.name(), "role", role.name());
    }
}
