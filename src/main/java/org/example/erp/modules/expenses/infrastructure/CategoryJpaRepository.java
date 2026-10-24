package org.example.erp.modules.expenses.infrastructure;

import org.example.erp.modules.expenses.domain.CategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface CategoryJpaRepository extends JpaRepository<CategoryRecord, Long> {
    List<CategoryRecord> findAllByOrderByNameAsc();

    List<CategoryRecord> findByKindAndNameIgnoreCase(CategoryKind kind, String name);
}
