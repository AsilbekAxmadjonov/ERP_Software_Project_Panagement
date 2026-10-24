package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.PeriodLockDTO;
import org.example.erp.modules.accounting.domain.PeriodLock;
import org.example.erp.modules.accounting.domain.PeriodLockRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.TransactionRunner;

import java.time.Clock;
import java.time.LocalDate;

/** Moves the lock date back, or removes it when lockDate is null. Restricted to the CEO at the HTTP boundary. */
public final class ReopenPeriod {
    private final PeriodLockRepository periods;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public ReopenPeriod(PeriodLockRepository periods, TransactionRunner tx, CurrentActor actor, Clock clock) {
        this.periods = periods;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public PeriodLockDTO handle(LocalDate lockDate) {
        return tx.run(() -> {
            PeriodLock lock = periods.get();
            lock.reopenTo(lockDate, actor.username(), clock.instant());
            return AccountingMapper.toDto(periods.save(lock));
        });
    }
}
