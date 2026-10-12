package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.AccountDTO;
import org.example.erp.modules.accounting.domain.AccountRepository;

import java.util.List;

public final class ListAccounts {
    private final AccountRepository accounts;

    public ListAccounts(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public List<AccountDTO> handle() {
        return accounts.findAllByCode().stream().map(AccountingMapper::toDto).toList();
    }
}
