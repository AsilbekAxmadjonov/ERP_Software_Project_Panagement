package org.example.erp.composition;

import org.example.erp.modules.accounting.application.actions.ListAccounts;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.expenses.application.contracts.LedgerAccounts;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.springframework.stereotype.Component;

import java.util.List;

/** Expense categories post to expense accounts; income categories post to revenue accounts. */
@Component
class ExpenseAccountsAdapter implements LedgerAccounts {
    private final ListAccounts listAccounts;

    ExpenseAccountsAdapter(ListAccounts listAccounts) {
        this.listAccounts = listAccounts;
    }

    @Override
    public List<AccountInfo> postable(EntryType type) {
        AccountKind kind = switch (type) {
            case EXPENSE -> AccountKind.EXPENSE;
            case INCOME -> AccountKind.REVENUE;
        };
        return listAccounts.handle().stream().filter(a -> a.active() && a.type() == kind)
                .map(a -> new AccountInfo(a.code(), a.name())).toList();
    }
}
