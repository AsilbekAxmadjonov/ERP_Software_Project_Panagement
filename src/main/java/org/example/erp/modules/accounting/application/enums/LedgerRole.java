package org.example.erp.modules.accounting.application.enums;

/** Well-known accounts other workflows post to, without knowing the chart of accounts layout. */
public enum LedgerRole {
    CASH("1000", "Cash (Kassa)", AccountKind.ASSET),
    BANK("1100", "Bank", AccountKind.ASSET),
    RECEIVABLE("1200", "Accounts Receivable", AccountKind.ASSET),
    INVENTORY("1300", "Inventory", AccountKind.ASSET),
    PAYABLE("2000", "Accounts Payable", AccountKind.LIABILITY),
    EQUITY("3000", "Owner's Equity", AccountKind.EQUITY),
    SALES_REVENUE("4000", "Sales Revenue", AccountKind.REVENUE),
    COST_OF_GOODS_SOLD("5000", "Cost of Goods Sold", AccountKind.EXPENSE),
    OPERATING_EXPENSES("5100", "Operating Expenses", AccountKind.EXPENSE),
    INVENTORY_LOSS("5200", "Inventory Loss / Adjustment", AccountKind.EXPENSE);

    private final String code;
    private final String defaultName;
    private final AccountKind kind;

    LedgerRole(String code, String defaultName, AccountKind kind) {
        this.code = code;
        this.defaultName = defaultName;
        this.kind = kind;
    }

    public String code() {
        return code;
    }

    public String defaultName() {
        return defaultName;
    }

    public AccountKind kind() {
        return kind;
    }
}
