package org.example.erp.modules.identity.domain;

import lombok.Getter;

@Getter
public final class UserAccount {
    private final Long id;
    private final String username;
    private String passwordHash;
    private String fullName;
    private Role role;
    private boolean active;

    private UserAccount(Long id, String username, String passwordHash, String fullName, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public static UserAccount register(String username, String passwordHash, String fullName, Role role) {
        return new UserAccount(null, username, passwordHash, fullName, role, true);
    }

    public static UserAccount restore(Long id, String username, String passwordHash, String fullName, Role role, boolean active) {
        return new UserAccount(id, username, passwordHash, fullName, role, active);
    }

    public void updateProfile(String fullName, Role role) {
        this.fullName = fullName;
        this.role = role;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
