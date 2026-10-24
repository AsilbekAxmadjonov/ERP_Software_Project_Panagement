package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.PeriodLockDTO;
import org.example.erp.modules.accounting.application.events.PeriodLockChanged;
import org.example.erp.modules.accounting.domain.PeriodLock;
import org.example.erp.modules.accounting.domain.PeriodLockRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;

import java.time.Clock;
import java.time.LocalDate;

/** Moves the lock date back, or removes it when lockDate is null. Restricted to the CEO at the HTTP boundary. */
public final class ReopenPeriod {
    private final PeriodLockRepository periods;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;
    private final IntegrationEventPublisher events;

    public ReopenPeriod(PeriodLockRepository periods, TransactionRunner tx, CurrentActor actor, Clock clock,
                        IntegrationEventPublisher events) {
        this.periods = periods;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
        this.events = events;
    }

    public PeriodLockDTO handle(LocalDate lockDate) {
        return tx.run(() -> {
            PeriodLock lock = periods.get();
            LocalDate previous = lock.getLockDate();
            lock.reopenTo(lockDate, actor.username(), clock.instant());
            PeriodLockDTO saved = AccountingMapper.toDto(periods.save(lock));
            events.publish(PeriodLockChanged.reopened(previous, saved.lockDate()));
            return saved;
        });
    }
}
