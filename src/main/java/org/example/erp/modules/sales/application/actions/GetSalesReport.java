package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.dto.SalesReportRowDTO;
import org.example.erp.modules.sales.domain.Invoice;
import org.example.erp.modules.sales.domain.InvoiceLine;
import org.example.erp.modules.sales.domain.InvoiceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** One row per invoice item, filtered by date range, customer and product (all filters optional). */
public final class GetSalesReport {
    private final InvoiceRepository invoices;

    public GetSalesReport(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public List<SalesReportRowDTO> handle(LocalDate from, LocalDate to, Long customerId, Long productId) {
        List<SalesReportRowDTO> rows = new ArrayList<>();
        for (Invoice invoice : invoices.findAllNewestFirst()) {
            if (from != null && invoice.getInvoiceDate().isBefore(from)) continue;
            if (to != null && invoice.getInvoiceDate().isAfter(to)) continue;
            if (customerId != null && invoice.getCustomerId() != customerId) continue;
            for (InvoiceLine line : invoice.getLines()) {
                if (productId != null && line.productId() != productId) continue;
                rows.add(new SalesReportRowDTO(invoice.getInvoiceDate(), invoice.getNumber(), invoice.getCustomerId(),
                        invoice.getCustomerName(), line.productId(), line.productName(), line.unit(), line.quantity(),
                        line.unitPrice(), line.lineTotal()));
            }
        }
        return rows;
    }
}
