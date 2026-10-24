package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.dto.CreditNoteDTO;
import org.example.erp.modules.sales.application.dto.CreditNoteLineDTO;
import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.application.dto.InvoiceLineDTO;
import org.example.erp.modules.sales.application.dto.PaymentDTO;
import org.example.erp.modules.sales.application.enums.PaymentChannel;
import org.example.erp.modules.sales.application.enums.PaymentProgress;
import org.example.erp.modules.sales.domain.CreditNote;
import org.example.erp.modules.sales.domain.Invoice;
import org.example.erp.modules.sales.domain.Payment;

final class SalesMapper {
    private SalesMapper() {}

    static InvoiceDTO toDto(Invoice i) {
        return new InvoiceDTO(i.getId(), i.getNumber(), i.getCustomerId(), i.getCustomerName(), i.getInvoiceDate(),
                i.getTotal(), i.getCost(), i.getPaid(), i.getCredited(), i.outstanding(), progress(i), i.getCreatedBy(),
                i.getLines().stream().map(l -> new InvoiceLineDTO(l.productId(), l.productName(), l.unit(), l.quantity(),
                        l.unitPrice(), l.lineTotal(), l.returnedQuantity(), l.returnable())).toList());
    }

    private static PaymentProgress progress(Invoice i) {
        if (i.outstanding().signum() == 0) return i.isFullyReturned() ? PaymentProgress.CREDITED : PaymentProgress.PAID;
        return i.getPaid().signum() == 0 && i.getCredited().signum() == 0 ? PaymentProgress.UNPAID : PaymentProgress.PARTIAL;
    }

    static CreditNoteDTO toDto(CreditNote c) {
        return new CreditNoteDTO(c.getId(), c.getNumber(), c.getInvoiceId(), c.getInvoiceNumber(), c.getCustomerId(),
                c.getCustomerName(), c.getDate(), c.getReason(), c.getTotal(), c.getCost(), c.getCreatedBy(),
                c.getLines().stream().map(l -> new CreditNoteLineDTO(l.productId(), l.productName(), l.unit(), l.quantity(),
                        l.unitPrice(), l.amount())).toList());
    }

    static PaymentDTO toDto(Payment p) {
        return new PaymentDTO(p.id(), p.customerId(), p.customerName(), p.invoiceId(), p.invoiceNumber(), p.date(),
                p.amount(), PaymentChannel.valueOf(p.method().name()), p.note(), p.createdBy());
    }
}
