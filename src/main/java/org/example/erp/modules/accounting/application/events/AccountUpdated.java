package org.example.erp.modules.accounting.application.events;

import org.example.erp.modules.accounting.application.dto.AccountDTO;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

/** Carries the account's state after a rename or (de)activation. */
public final class AccountUpdated extends BaseIntegrationEvent {
    private final AccountDTO account;

    public AccountUpdated(AccountDTO account) {
        super(null, null);
        this.account = account;
    }

    public AccountDTO account() {
        return account;
    }

    @Override
    public String eventName() {
        return "accounting.account-updated";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("accountId", account.id(), "code", account.code(), "accountName", account.name(),
                "type", account.type().name(), "active", account.active());
    }
}
