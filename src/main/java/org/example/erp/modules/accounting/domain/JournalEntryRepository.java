package org.example.erp.modules.accounting.domain;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository {
    Optional<JournalEntry> findById(long id);

    List<JournalEntry> findRecent();

    JournalEntry save(JournalEntry entry);
}
