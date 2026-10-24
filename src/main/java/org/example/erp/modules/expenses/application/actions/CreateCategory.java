package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.commands.CreateCategoryCommand;
import org.example.erp.modules.expenses.application.contracts.LedgerAccounts;
import org.example.erp.modules.expenses.application.dto.CategoryDTO;
import org.example.erp.modules.expenses.domain.Category;
import org.example.erp.modules.expenses.domain.CategoryKind;
import org.example.erp.modules.expenses.domain.CategoryRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.TransactionRunner;

public final class CreateCategory {
    private final CategoryRepository categories;
    private final LedgerAccounts ledger;
    private final TransactionRunner tx;

    public CreateCategory(CategoryRepository categories, LedgerAccounts ledger, TransactionRunner tx) {
        this.categories = categories;
        this.ledger = ledger;
        this.tx = tx;
    }

    public CategoryDTO handle(CreateCategoryCommand c) {
        return tx.run(() -> {
            CategoryKind kind = ExpensesMapper.toKind(c.type());
            Category category = Category.create(c.name(), kind, c.accountCode());
            if (categories.nameTaken(kind, category.getName(), null))
                throw new BusinessRuleException("A category named " + category.getName() + " already exists");
            ExpensesMapper.ensurePostable(ledger, c.type(), category.getAccountCode());
            return ExpensesMapper.toDto(categories.save(category));
        });
    }
}
