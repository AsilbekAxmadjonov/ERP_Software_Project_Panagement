package org.example.erp.modules.accounting.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface JournalEntryJpaRepository extends JpaRepository<JournalEntryRecord, Long> {
    List<JournalEntryRecord> findTop200ByOrderByEntryDateDescIdDesc();
}
