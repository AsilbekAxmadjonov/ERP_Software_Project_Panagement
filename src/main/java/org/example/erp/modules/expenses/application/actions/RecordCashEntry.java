package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.commands.RecordCashEntryCommand;
import org.example.erp.modules.expenses.application.dto.CashEntryDTO;
import org.example.erp.modules.expenses.application.events.CashEntryRecorded;
import org.example.erp.modules.expenses.domain.CashEntry;
import org.example.erp.modules.expenses.domain.CashEntryRepository;
import org.example.erp.modules.expenses.domain.Category;
import org.example.erp.modules.expenses.domain.CategoryRepository;
import org.example.erp.modules.expenses.domain.PaymentMethod;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/** Records an expense or other income; the ledger entry is posted in the same transaction by the event listener. */
public final class RecordCashEntry {
    private final CategoryRepository categories;
    private final CashEntryRepository entries;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public RecordCashEntry(CategoryRepository categories, CashEntryRepository entries, IntegrationEventPublisher events,
                           TransactionRunner tx, CurrentActor actor, Clock clock) {
        this.categories = categories;
        this.entries = entries;
        this.events = events;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public CashEntryDTO handle(RecordCashEntryCommand c) {
        LocalDate date = c.date() == null ? LocalDate.now(clock) : c.date();
        BigDecimal amount = c.amount().setScale(2, RoundingMode.HALF_UP);
        return tx.run(() -> {
            Category category = categories.findById(c.categoryId()).orElseThrow(() -> new NotFoundException("Category not found"));
            CashEntry saved = entries.save(CashEntry.record(ExpensesMapper.toKind(c.type()), category, date, amount,
                    PaymentMethod.valueOf(c.method().name()), c.description(), actor.username()));
            events.publish(new CashEntryRecorded(saved.reference(), c.type(), saved.getDate(), saved.getAmount(), c.method(),
                    saved.getAccountCode(), saved.getCategoryName(), saved.getDescription()));
            return ExpensesMapper.toDto(saved);
        });
    }
}
