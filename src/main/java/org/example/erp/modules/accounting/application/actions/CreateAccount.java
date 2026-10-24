package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.commands.CreateAccountCommand;
import org.example.erp.modules.accounting.application.dto.AccountDTO;
import org.example.erp.modules.accounting.application.events.AccountCreated;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.modules.accounting.domain.AccountType;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;

public final class CreateAccount {
    private final AccountRepository accounts;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;

    public CreateAccount(AccountRepository accounts, IntegrationEventPublisher events, TransactionRunner tx) {
        this.accounts = accounts;
        this.events = events;
        this.tx = tx;
    }

    public AccountDTO handle(CreateAccountCommand c) {
        String code = c.code().trim();
        return tx.run(() -> {
            if (accounts.findByCode(code).isPresent()) throw new BusinessRuleException("Account code already exists");
            Account account = Account.open(code, c.name().trim(), AccountType.valueOf(c.type().name()));
            AccountDTO saved = AccountingMapper.toDto(accounts.save(account));
            events.publish(new AccountCreated(saved));
            return saved;
        });
    }
}
