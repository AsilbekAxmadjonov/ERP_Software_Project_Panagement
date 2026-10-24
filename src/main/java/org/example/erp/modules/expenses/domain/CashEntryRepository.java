package org.example.erp.modules.expenses.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CashEntryRepository {
    Optional<CashEntry> findById(long id);

    List<CashEntry> findByKindNewestFirst(CategoryKind kind);

    /** Entries dated within the range, both ends inclusive; a null bound leaves that side open. */
    List<CashEntry> findDatedBetween(LocalDate from, LocalDate to);

    CashEntry save(CashEntry entry);
}
