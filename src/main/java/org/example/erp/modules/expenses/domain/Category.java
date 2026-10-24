package org.example.erp.modules.expenses.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

/** A user-managed classification of expenses or other income; each one posts to its own ledger account. */
@Getter
public final class Category {
    private final Long id;
    private String name;
    private final CategoryKind kind;
    private String accountCode;
    private boolean active;

    private Category(Long id, String name, CategoryKind kind, String accountCode, boolean active) {
        this.id = id;
        this.kind = kind;
        this.active = active;
        rename(name);
        mapTo(accountCode);
    }

    public static Category create(String name, CategoryKind kind, String accountCode) {
        return new Category(null, name, kind, accountCode, true);
    }

    public static Category restore(Long id, String name, CategoryKind kind, String accountCode, boolean active) {
        return new Category(id, name, kind, accountCode, active);
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) throw new BusinessRuleException("Category name is required");
        this.name = name.trim();
    }

    public void mapTo(String accountCode) {
        if (accountCode == null || accountCode.isBlank()) throw new BusinessRuleException("A ledger account is required");
        this.accountCode = accountCode.trim();
    }

    public void activate() {
        this.active = true;
    }

    public void archive() {
        this.active = false;
    }
}
