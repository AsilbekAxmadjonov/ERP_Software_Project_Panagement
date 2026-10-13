package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.domain.InvoiceRepository;

import java.util.List;

public final class ListInvoices {
    private final InvoiceRepository invoices;

    public ListInvoices(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public List<InvoiceDTO> handle() {
        return invoices.findAllNewestFirst().stream().map(SalesMapper::toDto).toList();
    }
}
