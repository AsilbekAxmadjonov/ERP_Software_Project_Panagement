package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.JournalEntryDTO;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.modules.accounting.domain.JournalEntryRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ListJournalEntries {
    private final AccountRepository accounts;
    private final JournalEntryRepository entries;

    public ListJournalEntries(AccountRepository accounts, JournalEntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    public List<JournalEntryDTO> handle() {
        Map<String, String> names = accounts.findAllByCode().stream().collect(Collectors.toMap(Account::getCode, Account::getName));
        return entries.findRecent().stream().map(e -> AccountingMapper.toDto(e, names)).toList();
    }
}
