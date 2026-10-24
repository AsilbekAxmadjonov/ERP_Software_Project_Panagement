package org.example.erp.modules.audit.domain;

import java.util.List;

public interface AuditEntryRepository {
    AuditEntry save(AuditEntry entry);

    /** Newest first, at most filter.limit() entries. */
    List<AuditEntry> search(AuditFilter filter);
}
