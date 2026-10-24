package org.example.erp.modules.expenses.infrastructure;

import org.example.erp.modules.expenses.domain.CategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

interface CashEntryJpaRepository extends JpaRepository<CashEntryRecord, Long> {
    List<CashEntryRecord> findByKindOrderByEntryDateDescIdDesc(CategoryKind kind);

    List<CashEntryRecord> findByEntryDateBetweenOrderByEntryDateAscIdAsc(LocalDate from, LocalDate to);

    List<CashEntryRecord> findByEntryDateGreaterThanEqualOrderByEntryDateAscIdAsc(LocalDate from);

    List<CashEntryRecord> findByEntryDateLessThanEqualOrderByEntryDateAscIdAsc(LocalDate to);

    List<CashEntryRecord> findAllByOrderByEntryDateAscIdAsc();
}
