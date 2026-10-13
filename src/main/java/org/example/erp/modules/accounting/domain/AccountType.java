package org.example.erp.modules.accounting.domain;

public enum AccountType {
    ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE;

    public boolean isDebitNormal() {
        return this == ASSET || this == EXPENSE;
    }
}
