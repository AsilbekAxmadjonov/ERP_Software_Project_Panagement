package org.example.erp.modules.accounting.domain;

import lombok.Getter;

@Getter
public final class Account {
    private final Long id;
    private final String code;
    private String name;
    private final AccountType type;
    private boolean active;

    private Account(Long id, String code, String name, AccountType type, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.active = active;
    }

    public static Account open(String code, String name, AccountType type) {
        return new Account(null, code, name, type, true);
    }

    public static Account restore(Long id, String code, String name, AccountType type, boolean active) {
        return new Account(id, code, name, type, active);
    }

    public void rename(String name) {
        this.name = name;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
