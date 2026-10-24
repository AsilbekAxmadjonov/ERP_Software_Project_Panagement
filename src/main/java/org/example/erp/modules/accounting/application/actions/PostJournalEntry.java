package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.dto.JournalEntryDTO;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.domain.*;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.TransactionRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PostJournalEntry {
    private final AccountRepository accounts;
    private final JournalEntryRepository entries;
    private final PeriodLockRepository periods;
    private final TransactionRunner tx;
    private final CurrentActor actor;

    public PostJournalEntry(AccountRepository accounts, JournalEntryRepository entries, PeriodLockRepository periods,
                            TransactionRunner tx, CurrentActor actor) {
        this.accounts = accounts;
        this.entries = entries;
        this.periods = periods;
        this.tx = tx;
        this.actor = actor;
    }

    public JournalEntryDTO handle(PostEntryCommand c, EntryKind kind) {
        return tx.run(() -> {
            periods.get().ensureOpen(c.date());
            Map<String, String> names = new HashMap<>();
            List<JournalLine> lines = c.lines().stream().map(l -> {
                Account account = accounts.findByCode(l.accountCode()).filter(Account::isActive)
                        .orElseThrow(() -> new BusinessRuleException("Unknown or inactive account " + l.accountCode()));
                names.put(account.getCode(), account.getName());
                return new JournalLine(account.getCode(), l.debit(), l.credit(), l.partnerId(), l.memo());
            }).toList();
            JournalEntry entry = JournalEntry.post(c.date(), c.reference(), c.description(),
                    EntryOrigin.valueOf(kind.name()), actor.username(), lines);
            return AccountingMapper.toDto(entries.save(entry), names);
        });
    }
}
