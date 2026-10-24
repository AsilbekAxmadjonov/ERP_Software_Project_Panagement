package org.example.erp.modules.employees.domain;

/**
 * What moved on an employee's account. Accrued salary and bonuses increase what the company owes the employee;
 * payments, advances and deductions decrease it. Payments and advances are paid out in cash or through the bank.
 */
public enum LedgerEntryType {
    SALARY_ACCRUED("SAL", 1, false),
    BONUS("BON", 1, false),
    PAYMENT("PAY", -1, true),
    ADVANCE("ADV", -1, true),
    DEDUCTION("DED", -1, false);

    private final String referencePrefix;
    private final int sign;
    private final boolean paidOut;

    LedgerEntryType(String referencePrefix, int sign, boolean paidOut) {
        this.referencePrefix = referencePrefix;
        this.sign = sign;
        this.paidOut = paidOut;
    }

    public String referencePrefix() {
        return referencePrefix;
    }

    /** +1 when the entry increases what is owed to the employee, -1 when it decreases it. */
    public int sign() {
        return sign;
    }

    /** Whether money leaves cash or bank, so a payment method is required. */
    public boolean paidOut() {
        return paidOut;
    }
}
