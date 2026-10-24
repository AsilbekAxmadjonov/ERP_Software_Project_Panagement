package org.example.erp.modules.employees.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * One movement on an employee's account; amount is always positive and the type decides its direction.
 * method is set only for payments and advances; period is the month a salary accrual is for.
 */
public record EmployeeLedgerEntry(Long id, long employeeId, LocalDate date, LedgerEntryType type, BigDecimal amount,
                                  PaymentMethod method, YearMonth period, String description, String createdBy) {

    public EmployeeLedgerEntry {
        if (amount.signum() <= 0) throw new BusinessRuleException("Amount must be greater than zero");
        if (type.paidOut() && method == null) throw new BusinessRuleException("Choose whether it is paid in cash or by bank");
        if (!type.paidOut() && method != null) throw new BusinessRuleException(type + " entries do not move cash");
        if ((type == LedgerEntryType.SALARY_ACCRUED) != (period != null))
            throw new BusinessRuleException("Only a salary accrual has a payroll period, and it always has one");
    }

    /** Positive when it increases what the company owes the employee. */
    public BigDecimal signedAmount() {
        return type.sign() > 0 ? amount : amount.negate();
    }

    /** Document number such as PAY-00012; only known once the entry has been saved. */
    public String reference() {
        if (id == null) throw new IllegalStateException("An unsaved entry has no reference yet");
        return String.format("%s-%05d", type.referencePrefix(), id);
    }
}
