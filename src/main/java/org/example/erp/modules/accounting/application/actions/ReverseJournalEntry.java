package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.JournalEntryDTO;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.modules.accounting.domain.JournalEntry;
import org.example.erp.modules.accounting.domain.JournalEntryRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

public final class ReverseJournalEntry {
    private final AccountRepository accounts;
    private final JournalEntryRepository entries;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public ReverseJournalEntry(AccountRepository accounts, JournalEntryRepository entries, TransactionRunner tx,
                               CurrentActor actor, Clock clock) {
        this.accounts = accounts;
        this.entries = entries;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public JournalEntryDTO handle(long entryId) {
        return tx.run(() -> {
            JournalEntry original = entries.findById(entryId).orElseThrow(() -> new NotFoundException("Entry not found"));
            JournalEntry reversal = original.reversal(LocalDate.now(clock), actor.username());
            original.markReversed();
            entries.save(original);
            return AccountingMapper.toDto(entries.save(reversal),
                    accounts.findAllByCode().stream().collect(Collectors.toMap(Account::getCode, Account::getName)));
        });
    }
}
