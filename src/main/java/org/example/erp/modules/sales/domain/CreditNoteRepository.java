package org.example.erp.modules.sales.domain;

import java.util.List;

public interface CreditNoteRepository {
    CreditNote save(CreditNote creditNote);

    List<CreditNote> findAllNewestFirst();

    List<CreditNote> findByInvoice(long invoiceId);
}
