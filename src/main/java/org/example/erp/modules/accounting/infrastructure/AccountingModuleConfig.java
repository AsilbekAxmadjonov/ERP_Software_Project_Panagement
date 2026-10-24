package org.example.erp.modules.accounting.infrastructure;

import org.example.erp.modules.accounting.application.actions.*;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.modules.accounting.domain.AccountType;
import org.example.erp.modules.accounting.domain.JournalEntryRepository;
import org.example.erp.modules.accounting.domain.PeriodLockRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AccountingModuleConfig {

    @Bean
    ListAccounts listAccounts(AccountRepository accounts) {
        return new ListAccounts(accounts);
    }

    @Bean
    CreateAccount createAccount(AccountRepository accounts, IntegrationEventPublisher events, TransactionRunner tx) {
        return new CreateAccount(accounts, events, tx);
    }

    @Bean
    UpdateAccount updateAccount(AccountRepository accounts, IntegrationEventPublisher events, TransactionRunner tx) {
        return new UpdateAccount(accounts, events, tx);
    }

    @Bean
    public PostJournalEntry postJournalEntry(AccountRepository accounts, JournalEntryRepository entries,
                                             PeriodLockRepository periods, TransactionRunner tx, CurrentActor actor,
                                             IntegrationEventPublisher events) {
        return new PostJournalEntry(accounts, entries, periods, tx, actor, events);
    }

    @Bean
    ReverseJournalEntry reverseJournalEntry(AccountRepository accounts, JournalEntryRepository entries,
                                            PeriodLockRepository periods, TransactionRunner tx, CurrentActor actor, Clock clock,
                                            IntegrationEventPublisher events) {
        return new ReverseJournalEntry(accounts, entries, periods, tx, actor, clock, events);
    }

    @Bean
    GetPeriodLock getPeriodLock(PeriodLockRepository periods) {
        return new GetPeriodLock(periods);
    }

    @Bean
    public ClosePeriod closePeriod(PeriodLockRepository periods, TransactionRunner tx, CurrentActor actor, Clock clock,
                                   IntegrationEventPublisher events) {
        return new ClosePeriod(periods, tx, actor, clock, events);
    }

    @Bean
    public ReopenPeriod reopenPeriod(PeriodLockRepository periods, TransactionRunner tx, CurrentActor actor, Clock clock,
                                     IntegrationEventPublisher events) {
        return new ReopenPeriod(periods, tx, actor, clock, events);
    }

    @Bean
    ListJournalEntries listJournalEntries(AccountRepository accounts, JournalEntryRepository entries) {
        return new ListJournalEntries(accounts, entries);
    }

    @Bean
    GetTrialBalance getTrialBalance(AccountRepository accounts, LedgerReadModel ledger) {
        return new GetTrialBalance(accounts, ledger);
    }

    @Bean
    GetAccountLedger getAccountLedger(AccountRepository accounts, LedgerReadModel ledger) {
        return new GetAccountLedger(accounts, ledger);
    }

    @Bean
    GetPartnerLedger getPartnerLedger(LedgerReadModel ledger) {
        return new GetPartnerLedger(ledger);
    }

    @Bean
    public GetPartnerBalance getPartnerBalance(LedgerReadModel ledger) {
        return new GetPartnerBalance(ledger);
    }

    /**
     * Default chart of accounts: reference data every posting workflow relies on. Opens any well-known account that is
     * missing, so databases created before a role was added get it too; existing accounts are left as they are.
     */
    @Bean
    CommandLineRunner bootstrapChartOfAccounts(AccountRepository accounts) {
        return args -> {
            for (LedgerRole role : LedgerRole.values()) {
                if (accounts.findByCode(role.code()).isPresent()) continue;
                accounts.save(Account.open(role.code(), role.defaultName(), AccountType.valueOf(role.kind().name())));
            }
        };
    }
}
