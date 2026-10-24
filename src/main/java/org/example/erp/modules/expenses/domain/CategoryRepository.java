package org.example.erp.modules.expenses.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(long id);

    List<Category> findAllByName();

    /** Whether another category of the same kind already uses this name (case-insensitive). */
    boolean nameTaken(CategoryKind kind, String name, Long exceptId);

    long count();

    Category save(Category category);
}
