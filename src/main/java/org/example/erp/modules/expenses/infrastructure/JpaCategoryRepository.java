package org.example.erp.modules.expenses.infrastructure;

import org.example.erp.modules.expenses.domain.Category;
import org.example.erp.modules.expenses.domain.CategoryKind;
import org.example.erp.modules.expenses.domain.CategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
class JpaCategoryRepository implements CategoryRepository {
    private final CategoryJpaRepository jpa;

    JpaCategoryRepository(CategoryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Category> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Category> findAllByName() {
        return jpa.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean nameTaken(CategoryKind kind, String name, Long exceptId) {
        return jpa.findByKindAndNameIgnoreCase(kind, name).stream().anyMatch(r -> !Objects.equals(r.getId(), exceptId));
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public Category save(Category c) {
        CategoryRecord r = c.getId() == null ? new CategoryRecord() : jpa.getReferenceById(c.getId());
        r.setName(c.getName());
        r.setKind(c.getKind());
        r.setAccountCode(c.getAccountCode());
        r.setActive(c.isActive());
        return toDomain(jpa.save(r));
    }

    private Category toDomain(CategoryRecord r) {
        return Category.restore(r.getId(), r.getName(), r.getKind(), r.getAccountCode(), r.isActive());
    }
}
