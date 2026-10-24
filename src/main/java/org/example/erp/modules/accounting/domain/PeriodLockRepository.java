package org.example.erp.modules.accounting.domain;

public interface PeriodLockRepository {
    /** The single company-wide lock; an open lock when none was ever set. */
    PeriodLock get();

    PeriodLock save(PeriodLock lock);
}
