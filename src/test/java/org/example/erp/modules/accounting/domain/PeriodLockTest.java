package org.example.erp.modules.accounting.domain;

import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PeriodLockTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);
    private static final Instant NOW = Instant.parse("2026-10-25T10:00:00Z");

    @Test
    void entriesOnOrBeforeTheLockDateAreRejected() {
        PeriodLock lock = PeriodLock.open();
        lock.ensureOpen(LocalDate.of(2020, 1, 1));

        lock.closeThrough(LocalDate.of(2026, 9, 30), TODAY, "ceo", NOW);

        assertThrows(BusinessRuleException.class, () -> lock.ensureOpen(LocalDate.of(2026, 9, 30)));
        assertThrows(BusinessRuleException.class, () -> lock.ensureOpen(LocalDate.of(2026, 9, 1)));
        lock.ensureOpen(LocalDate.of(2026, 10, 1));
    }

    @Test
    void closingOnlyMovesForwardAndNeverIntoTheFuture() {
        PeriodLock lock = PeriodLock.open();
        assertThrows(BusinessRuleException.class, () -> lock.closeThrough(TODAY.plusDays(1), TODAY, "ceo", NOW));
        lock.closeThrough(LocalDate.of(2026, 9, 30), TODAY, "ceo", NOW);
        assertThrows(BusinessRuleException.class, () -> lock.closeThrough(LocalDate.of(2026, 9, 30), TODAY, "ceo", NOW));
    }

    @Test
    void reopeningMovesTheLockBackOrRemovesIt() {
        PeriodLock lock = PeriodLock.open();
        assertThrows(BusinessRuleException.class, () -> lock.reopenTo(null, "ceo", NOW));
        lock.closeThrough(LocalDate.of(2026, 9, 30), TODAY, "ceo", NOW);

        assertThrows(BusinessRuleException.class, () -> lock.reopenTo(LocalDate.of(2026, 10, 5), "ceo", NOW));
        lock.reopenTo(LocalDate.of(2026, 8, 31), "ceo", NOW);
        assertEquals(LocalDate.of(2026, 8, 31), lock.getLockDate());
        lock.reopenTo(null, "ceo", NOW);
        assertNull(lock.getLockDate());
    }
}
