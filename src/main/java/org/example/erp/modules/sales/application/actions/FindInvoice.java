package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.domain.InvoiceRepository;
import org.example.erp.platform.NotFoundException;

public final class FindInvoice {
    private final InvoiceRepository invoices;

    public FindInvoice(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public InvoiceDTO handle(long id) {
        return invoices.findById(id).map(SalesMapper::toDto).orElseThrow(() -> new NotFoundException("Invoice not found"));
    }
}
