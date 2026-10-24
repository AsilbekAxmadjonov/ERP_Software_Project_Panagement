package org.example.erp.modules.accounting.application.events;

import org.example.erp.modules.accounting.application.dto.JournalEntryDTO;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/** reversal is the new entry that cancels the original one. */
public final class JournalEntryReversed extends BaseIntegrationEvent {
    private final JournalEntryDTO reversal;

    public JournalEntryReversed(JournalEntryDTO reversal) {
        super(reversal.reference(), null);
        this.reversal = reversal;
    }

    public JournalEntryDTO reversal() {
        return reversal;
    }

    @Override
    public String eventName() {
        return "accounting.journal-entry-reversed";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("entryId", reversal.reversalOf());
        payload.put("reversalId", reversal.id());
        payload.put("reference", reversal.reference());
        payload.put("entryDate", reversal.entryDate().toString());
        payload.put("total", reversal.total().toPlainString());
        return payload;
    }
}
