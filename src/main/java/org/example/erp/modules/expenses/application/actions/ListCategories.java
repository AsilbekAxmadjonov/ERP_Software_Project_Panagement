package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.dto.CategoryDTO;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.domain.CategoryRepository;

import java.util.List;

public final class ListCategories {
    private final CategoryRepository categories;

    public ListCategories(CategoryRepository categories) {
        this.categories = categories;
    }

    /** type is optional: all categories are listed when absent. */
    public List<CategoryDTO> handle(EntryType type) {
        return categories.findAllByName().stream()
                .filter(c -> type == null || c.getKind() == ExpensesMapper.toKind(type))
                .map(ExpensesMapper::toDto).toList();
    }
}
