package org.example.erp.modules.identity.application.events;

import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/** The user's state after an edit. passwordChanged says whether a new password was set; the password itself is never included. */
public final class UserUpdated extends BaseIntegrationEvent {
    private final long userId;
    private final String username;
    private final String fullName;
    private final UserRole role;
    private final boolean active;
    private final boolean passwordChanged;

    public UserUpdated(long userId, String username, String fullName, UserRole role, boolean active, boolean passwordChanged) {
        super(username, null);
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
        this.passwordChanged = passwordChanged;
    }

    public long userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public boolean passwordChanged() {
        return passwordChanged;
    }

    @Override
    public String eventName() {
        return "identity.user-updated";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("fullName", fullName);
        payload.put("role", role.name());
        payload.put("active", active);
        payload.put("passwordChanged", passwordChanged);
        return payload;
    }
}
