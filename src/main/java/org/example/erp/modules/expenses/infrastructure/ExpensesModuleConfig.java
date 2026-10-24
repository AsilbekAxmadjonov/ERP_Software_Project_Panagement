package org.example.erp.modules.expenses.infrastructure;

import org.example.erp.modules.expenses.application.actions.*;
import org.example.erp.modules.expenses.application.contracts.LedgerAccounts;
import org.example.erp.modules.expenses.domain.CashEntryRepository;
import org.example.erp.modules.expenses.domain.Category;
import org.example.erp.modules.expenses.domain.CategoryKind;
import org.example.erp.modules.expenses.domain.CategoryRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class ExpensesModuleConfig {

    @Bean
    ListCategories listExpenseCategories(CategoryRepository categories) {
        return new ListCategories(categories);
    }

    @Bean
    CreateCategory createExpenseCategory(CategoryRepository categories, LedgerAccounts ledger, TransactionRunner tx) {
        return new CreateCategory(categories, ledger, tx);
    }

    @Bean
    UpdateCategory updateExpenseCategory(CategoryRepository categories, LedgerAccounts ledger, TransactionRunner tx) {
        return new UpdateCategory(categories, ledger, tx);
    }

    @Bean
    ListLedgerAccounts listExpenseLedgerAccounts(LedgerAccounts ledger) {
        return new ListLedgerAccounts(ledger);
    }

    @Bean
    RecordCashEntry recordCashEntry(CategoryRepository categories, CashEntryRepository entries, IntegrationEventPublisher events,
                                    TransactionRunner tx, CurrentActor actor, Clock clock) {
        return new RecordCashEntry(categories, entries, events, tx, actor, clock);
    }

    @Bean
    ListCashEntries listCashEntries(CashEntryRepository entries) {
        return new ListCashEntries(entries);
    }

    @Bean
    FindCashEntry findCashEntry(CashEntryRepository entries) {
        return new FindCashEntry(entries);
    }

    @Bean
    CancelCashEntry cancelCashEntry(CashEntryRepository entries, IntegrationEventPublisher events, TransactionRunner tx, Clock clock) {
        return new CancelCashEntry(entries, events, tx, clock);
    }

    @Bean
    GetCategoryReport getCategoryReport(CashEntryRepository entries) {
        return new GetCategoryReport(entries);
    }

    private record DefaultCategory(String name, CategoryKind kind, String accountCode) {}

    /** Account codes match the expense and revenue accounts seeded in the default chart of accounts. */
    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("Rent", CategoryKind.EXPENSE, "6100"),
            new DefaultCategory("Utilities", CategoryKind.EXPENSE, "6200"),
            new DefaultCategory("Transport", CategoryKind.EXPENSE, "6300"),
            new DefaultCategory("Office supplies", CategoryKind.EXPENSE, "6400"),
            new DefaultCategory("Other expenses", CategoryKind.EXPENSE, "6900"),
            new DefaultCategory("Interest", CategoryKind.INCOME, "4100"),
            new DefaultCategory("Service income", CategoryKind.INCOME, "4100"),
            new DefaultCategory("Other income", CategoryKind.INCOME, "4100"));

    /** Starter categories for an empty database; afterwards they are managed by users. */
    @Bean
    CommandLineRunner bootstrapExpenseCategories(CategoryRepository categories) {
        return args -> {
            if (categories.count() > 0) return;
            for (DefaultCategory c : DEFAULT_CATEGORIES) categories.save(Category.create(c.name(), c.kind(), c.accountCode()));
        };
    }
}
