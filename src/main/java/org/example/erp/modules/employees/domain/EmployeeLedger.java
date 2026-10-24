package org.example.erp.modules.employees.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/**
 * An employee's account with the company. The balance is what the company owes the employee: positive after salary
 * is accrued, negative when the employee has received more (an advance) than has been earned.
 */
public final class EmployeeLedger {
    private static final Comparator<EmployeeLedgerEntry> CHRONOLOGICAL = Comparator.comparing(EmployeeLedgerEntry::date)
            .thenComparing(e -> e.id() == null ? Long.MAX_VALUE : e.id());

    private final Employee employee;
    private final List<EmployeeLedgerEntry> entries;

    public EmployeeLedger(Employee employee, List<EmployeeLedgerEntry> entries) {
        this.employee = employee;
        this.entries = entries.stream().sorted(CHRONOLOGICAL).toList();
    }

    /** Entries oldest first. */
    public List<EmployeeLedgerEntry> entries() {
        return entries;
    }

    public BigDecimal balance() {
        return sum(entries);
    }

    /** What was owed at the start of the given day. */
    public BigDecimal balanceBefore(LocalDate date) {
        return sum(entries.stream().filter(e -> e.date().isBefore(date)).toList());
    }

    /**
     * Validates a new movement against the account and returns it, ready to be saved. Salary, bonuses and advances
     * are only for active employees; a month's salary is accrued once; payments and deductions cannot exceed what is
     * owed, so anything paid beyond that has to be recorded as an advance.
     */
    public EmployeeLedgerEntry record(LedgerEntryType type, LocalDate date, BigDecimal amount, PaymentMethod method, YearMonth period,
                                      String description, String createdBy) {
        String text = description == null || description.isBlank() ? null : description.trim();
        EmployeeLedgerEntry entry = new EmployeeLedgerEntry(null, employee.getId(), date, type, amount, method, period, text, createdBy);
        switch (type) {
            case SALARY_ACCRUED -> {
                ensureActive("Salary");
                if (entries.stream().anyMatch(e -> e.type() == LedgerEntryType.SALARY_ACCRUED && period.equals(e.period())))
                    throw new BusinessRuleException("Salary for " + period + " is already accrued for " + employee.getFullName());
            }
            case BONUS -> ensureActive("A bonus");
            case ADVANCE -> ensureActive("An advance");
            case PAYMENT -> {
                if (amount.compareTo(balance()) > 0)
                    throw new BusinessRuleException("Payment exceeds the " + balance().max(BigDecimal.ZERO) + " owed to "
                            + employee.getFullName() + "; record the excess as an advance");
            }
            case DEDUCTION -> {
                if (amount.compareTo(balance()) > 0)
                    throw new BusinessRuleException("Deduction exceeds the " + balance().max(BigDecimal.ZERO) + " owed to " + employee.getFullName());
            }
        }
        return entry;
    }

    private void ensureActive(String what) {
        if (!employee.isActive()) throw new BusinessRuleException(what + " cannot be recorded for an inactive employee");
    }

    private static BigDecimal sum(List<EmployeeLedgerEntry> entries) {
        return entries.stream().map(EmployeeLedgerEntry::signedAmount).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
