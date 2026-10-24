package org.example.erp.modules.audit.application.actions;

import org.example.erp.modules.audit.application.dto.AuditEntryDTO;
import org.example.erp.modules.audit.application.queries.AuditQuery;
import org.example.erp.modules.audit.domain.AuditEntry;
import org.example.erp.modules.audit.domain.AuditEntryRepository;
import org.example.erp.modules.audit.domain.AuditFilter;
import org.example.erp.platform.BusinessRuleException;

import java.util.List;

/** Newest entries first, capped so an unfiltered request on a long-lived database stays cheap. */
public final class ListAuditEntries {
    public static final int DEFAULT_LIMIT = 500;
    public static final int MAX_LIMIT = 2000;

    private final AuditEntryRepository entries;

    public ListAuditEntries(AuditEntryRepository entries) {
        this.entries = entries;
    }

    public List<AuditEntryDTO> handle(AuditQuery q) {
        if (q.from() != null && q.to() != null && !q.from().isBefore(q.to())) throw new BusinessRuleException("'from' must be before 'to'");
        int limit = q.limit() == null ? DEFAULT_LIMIT : Math.max(1, Math.min(q.limit(), MAX_LIMIT));
        AuditFilter filter = new AuditFilter(q.from(), q.to(), blankToNull(q.actor()), blankToNull(q.action()), limit);
        return entries.search(filter).stream().map(ListAuditEntries::toDto).toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static AuditEntryDTO toDto(AuditEntry e) {
        return new AuditEntryDTO(e.getId(), e.getOccurredAt(), e.getActor(), e.getAction(), e.getSummary(), e.getSubject(),
                e.getPayload());
    }
}
