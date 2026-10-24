package org.example.erp.modules.accounting.application.events;

import org.example.erp.modules.accounting.application.dto.AccountDTO;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

public final class AccountCreated extends BaseIntegrationEvent {
    private final AccountDTO account;

    public AccountCreated(AccountDTO account) {
        super(null, null);
        this.account = account;
    }

    public AccountDTO account() {
        return account;
    }

    @Override
    public String eventName() {
        return "accounting.account-created";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("accountId", account.id(), "code", account.code(), "accountName", account.name(),
                "type", account.type().name(), "active", account.active());
    }
}
