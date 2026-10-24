package org.example.erp.modules.accounting.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.time.Instant;
import java.time.LocalDate;

/** Entries dated on or before the lock date belong to a closed period and cannot be posted. */
@Getter
public final class PeriodLock {
    private LocalDate lockDate;
    private String changedBy;
    private Instant changedAt;

    private PeriodLock(LocalDate lockDate, String changedBy, Instant changedAt) {
        this.lockDate = lockDate;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public static PeriodLock open() {
        return new PeriodLock(null, null, null);
    }

    public static PeriodLock restore(LocalDate lockDate, String changedBy, Instant changedAt) {
        return new PeriodLock(lockDate, changedBy, changedAt);
    }

    public void ensureOpen(LocalDate entryDate) {
        if (lockDate != null && !entryDate.isAfter(lockDate))
            throw new BusinessRuleException("The accounting period up to " + lockDate + " is closed; "
                    + "entries dated " + entryDate + " are not allowed");
    }

    public void closeThrough(LocalDate date, LocalDate today, String by, Instant at) {
        if (date.isAfter(today)) throw new BusinessRuleException("Cannot close a period that ends in the future");
        if (lockDate != null && !date.isAfter(lockDate))
            throw new BusinessRuleException("The period is already closed up to " + lockDate);
        change(date, by, at);
    }

    /** date is the new, earlier lock date, or null to open every period. */
    public void reopenTo(LocalDate date, String by, Instant at) {
        if (lockDate == null) throw new BusinessRuleException("No period is closed");
        if (date != null && !date.isBefore(lockDate))
            throw new BusinessRuleException("The new lock date must be before the current one (" + lockDate + ")");
        change(date, by, at);
    }

    private void change(LocalDate date, String by, Instant at) {
        this.lockDate = date;
        this.changedBy = by;
        this.changedAt = at;
    }
}
