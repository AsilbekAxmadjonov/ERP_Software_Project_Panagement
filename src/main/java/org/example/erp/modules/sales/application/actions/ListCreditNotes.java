package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.dto.CreditNoteDTO;
import org.example.erp.modules.sales.domain.CreditNoteRepository;

import java.util.List;

public final class ListCreditNotes {
    private final CreditNoteRepository creditNotes;

    public ListCreditNotes(CreditNoteRepository creditNotes) {
        this.creditNotes = creditNotes;
    }

    /** invoiceId is optional; without it every credit note is listed. */
    public List<CreditNoteDTO> handle(Long invoiceId) {
        var found = invoiceId == null ? creditNotes.findAllNewestFirst() : creditNotes.findByInvoice(invoiceId);
        return found.stream().map(SalesMapper::toDto).toList();
    }
}
