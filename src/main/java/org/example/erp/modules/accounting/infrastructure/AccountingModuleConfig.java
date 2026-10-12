package org.example.erp.modules.accounting.infrastructure;

import org.example.erp.modules.accounting.application.actions.*;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.modules.accounting.domain.AccountType;
import org.example.erp.modules.accounting.domain.JournalEntryRepository;
import org.example.erp.platform.CurrentActor;
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
    CreateAccount createAccount(AccountRepository accounts, TransactionRunner tx) {
        return new CreateAccount(accounts, tx);
    }

    @Bean
    UpdateAccount updateAccount(AccountRepository accounts, TransactionRunner tx) {
        return new UpdateAccount(accounts, tx);
    }

    @Bean
    public PostJournalEntry postJournalEntry(AccountRepository accounts, JournalEntryRepository entries, TransactionRunner tx, CurrentActor actor) {
        return new PostJournalEntry(accounts, entries, tx, actor);
    }

    @Bean
    ReverseJournalEntry reverseJournalEntry(AccountRepository accounts, JournalEntryRepository entries, TransactionRunner tx,
                                            CurrentActor actor, Clock clock) {
        return new ReverseJournalEntry(accounts, entries, tx, actor, clock);
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

    /** Default chart of accounts for an empty database: reference data every posting workflow relies on. */
    @Bean
    CommandLineRunner bootstrapChartOfAccounts(AccountRepository accounts) {
        return args -> {
            if (accounts.count() > 0) return;
            for (LedgerRole role : LedgerRole.values()) {
                accounts.save(Account.open(role.code(), role.defaultName(), AccountType.valueOf(role.kind().name())));
            }
        };
    }
}
