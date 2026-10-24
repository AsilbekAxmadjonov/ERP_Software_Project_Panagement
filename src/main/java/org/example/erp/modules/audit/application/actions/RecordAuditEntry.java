package org.example.erp.modules.audit.application.actions;

import org.example.erp.modules.audit.application.commands.RecordAuditEntryCommand;
import org.example.erp.modules.audit.domain.AuditEntry;
import org.example.erp.modules.audit.domain.AuditEntryRepository;

public final class RecordAuditEntry {
    private final AuditEntryRepository entries;

    public RecordAuditEntry(AuditEntryRepository entries) {
        this.entries = entries;
    }

    public void handle(RecordAuditEntryCommand c) {
        entries.save(AuditEntry.record(c.occurredAt(), c.actor(), c.action(), c.payload()));
    }
}
