package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.AccountDTO;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

public final class UpdateAccount {
    private final AccountRepository accounts;
    private final TransactionRunner tx;

    public UpdateAccount(AccountRepository accounts, TransactionRunner tx) {
        this.accounts = accounts;
        this.tx = tx;
    }

    /** Null fields are left unchanged. */
    public AccountDTO handle(String code, String name, Boolean active) {
        return tx.run(() -> {
            Account account = accounts.findByCode(code).orElseThrow(() -> new NotFoundException("Account not found"));
            if (name != null) account.rename(name.trim());
            if (active != null) {
                if (active) account.activate(); else account.deactivate();
            }
            return AccountingMapper.toDto(accounts.save(account));
        });
    }
}
