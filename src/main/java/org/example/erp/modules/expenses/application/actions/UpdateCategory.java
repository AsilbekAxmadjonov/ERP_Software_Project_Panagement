package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.commands.UpdateCategoryCommand;
import org.example.erp.modules.expenses.application.contracts.LedgerAccounts;
import org.example.erp.modules.expenses.application.dto.CategoryDTO;
import org.example.erp.modules.expenses.domain.Category;
import org.example.erp.modules.expenses.domain.CategoryRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

/** Changes apply to entries recorded from now on; existing entries keep the name and account they were posted with. */
public final class UpdateCategory {
    private final CategoryRepository categories;
    private final LedgerAccounts ledger;
    private final TransactionRunner tx;

    public UpdateCategory(CategoryRepository categories, LedgerAccounts ledger, TransactionRunner tx) {
        this.categories = categories;
        this.ledger = ledger;
        this.tx = tx;
    }

    public CategoryDTO handle(long id, UpdateCategoryCommand c) {
        return tx.run(() -> {
            Category category = categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
            category.rename(c.name());
            if (categories.nameTaken(category.getKind(), category.getName(), category.getId()))
                throw new BusinessRuleException("A category named " + category.getName() + " already exists");
            if (!category.getAccountCode().equals(c.accountCode() == null ? null : c.accountCode().trim())) {
                category.mapTo(c.accountCode());
                ExpensesMapper.ensurePostable(ledger, ExpensesMapper.toType(category.getKind()), category.getAccountCode());
            }
            if (c.active()) category.activate();
            else category.archive();
            return ExpensesMapper.toDto(categories.save(category));
        });
    }
}
