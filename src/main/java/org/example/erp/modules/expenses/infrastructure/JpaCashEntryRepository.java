package org.example.erp.modules.expenses.infrastructure;

import org.example.erp.modules.expenses.domain.CashEntry;
import org.example.erp.modules.expenses.domain.CashEntryRepository;
import org.example.erp.modules.expenses.domain.CategoryKind;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
class JpaCashEntryRepository implements CashEntryRepository {
    private final CashEntryJpaRepository jpa;

    JpaCashEntryRepository(CashEntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<CashEntry> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<CashEntry> findByKindNewestFirst(CategoryKind kind) {
        return jpa.findByKindOrderByEntryDateDescIdDesc(kind).stream().map(this::toDomain).toList();
    }

    @Override
    public List<CashEntry> findDatedBetween(LocalDate from, LocalDate to) {
        List<CashEntryRecord> records;
        if (from != null && to != null) records = jpa.findByEntryDateBetweenOrderByEntryDateAscIdAsc(from, to);
        else if (from != null) records = jpa.findByEntryDateGreaterThanEqualOrderByEntryDateAscIdAsc(from);
        else if (to != null) records = jpa.findByEntryDateLessThanEqualOrderByEntryDateAscIdAsc(to);
        else records = jpa.findAllByOrderByEntryDateAscIdAsc();
        return records.stream().map(this::toDomain).toList();
    }

    @Override
    public CashEntry save(CashEntry e) {
        // everything but the cancellation is fixed when the entry is recorded
        CashEntryRecord r = e.getId() == null ? new CashEntryRecord() : jpa.getReferenceById(e.getId());
        r.setKind(e.getKind());
        r.setCategoryId(e.getCategoryId());
        r.setCategoryName(e.getCategoryName());
        r.setAccountCode(e.getAccountCode());
        r.setEntryDate(e.getDate());
        r.setAmount(e.getAmount());
        r.setMethod(e.getMethod());
        r.setDescription(e.getDescription());
        r.setStatus(e.getStatus());
        r.setCancelledOn(e.getCancelledOn());
        r.setCreatedBy(e.getCreatedBy());
        return toDomain(jpa.save(r));
    }

    private CashEntry toDomain(CashEntryRecord r) {
        return CashEntry.restore(r.getId(), r.getKind(), r.getCategoryId(), r.getCategoryName(), r.getAccountCode(), r.getEntryDate(),
                r.getAmount(), r.getMethod(), r.getDescription(), r.getStatus(), r.getCancelledOn(), r.getCreatedBy());
    }
}
