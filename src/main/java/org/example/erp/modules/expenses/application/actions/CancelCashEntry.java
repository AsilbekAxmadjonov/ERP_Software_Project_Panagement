package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.dto.CashEntryDTO;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;
import org.example.erp.modules.expenses.application.events.CashEntryCancelled;
import org.example.erp.modules.expenses.domain.CashEntry;
import org.example.erp.modules.expenses.domain.CashEntryRepository;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.time.Clock;
import java.time.LocalDate;

/** Cancels an entry and has its ledger posting reversed on the cancellation date, which must be in an open period. */
public final class CancelCashEntry {
    private final CashEntryRepository entries;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final Clock clock;

    public CancelCashEntry(CashEntryRepository entries, IntegrationEventPublisher events, TransactionRunner tx, Clock clock) {
        this.entries = entries;
        this.events = events;
        this.tx = tx;
        this.clock = clock;
    }

    /** date is optional: today is used when absent. */
    public CashEntryDTO handle(long id, LocalDate date) {
        LocalDate on = date == null ? LocalDate.now(clock) : date;
        return tx.run(() -> {
            CashEntry entry = entries.findById(id).orElseThrow(() -> new NotFoundException("Entry not found"));
            entry.cancel(on);
            CashEntry saved = entries.save(entry);
            events.publish(new CashEntryCancelled(saved.reference(), ExpensesMapper.toType(saved.getKind()), on, saved.getAmount(),
                    PaymentChannel.valueOf(saved.getMethod().name()), saved.getAccountCode(), saved.getCategoryName()));
            return ExpensesMapper.toDto(saved);
        });
    }
}
