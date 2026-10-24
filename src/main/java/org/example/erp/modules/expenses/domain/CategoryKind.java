package org.example.erp.modules.expenses.domain;

/** Whether a category classifies money spent (an expense) or money received outside sales (other income). */
public enum CategoryKind {
    EXPENSE("EXP"),
    INCOME("INC");

    private final String referencePrefix;

    CategoryKind(String referencePrefix) {
        this.referencePrefix = referencePrefix;
    }

    public String referencePrefix() {
        return referencePrefix;
    }
}
