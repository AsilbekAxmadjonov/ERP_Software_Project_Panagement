package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.application.dto.InvoiceLineDTO;
import org.example.erp.modules.sales.application.dto.PaymentDTO;
import org.example.erp.modules.sales.application.enums.PaymentChannel;
import org.example.erp.modules.sales.application.enums.PaymentProgress;
import org.example.erp.modules.sales.domain.Invoice;
import org.example.erp.modules.sales.domain.Payment;

final class SalesMapper {
    private SalesMapper() {}

    static InvoiceDTO toDto(Invoice i) {
        PaymentProgress status = i.getPaid().signum() == 0 ? PaymentProgress.UNPAID
                : i.outstanding().signum() == 0 ? PaymentProgress.PAID : PaymentProgress.PARTIAL;
        return new InvoiceDTO(i.getId(), i.getNumber(), i.getCustomerId(), i.getCustomerName(), i.getInvoiceDate(),
                i.getTotal(), i.getCost(), i.getPaid(), i.outstanding(), status, i.getCreatedBy(),
                i.getLines().stream().map(l -> new InvoiceLineDTO(l.productId(), l.productName(), l.unit(), l.quantity(),
                        l.unitPrice(), l.lineTotal())).toList());
    }

    static PaymentDTO toDto(Payment p) {
        return new PaymentDTO(p.id(), p.customerId(), p.customerName(), p.invoiceId(), p.invoiceNumber(), p.date(),
                p.amount(), PaymentChannel.valueOf(p.method().name()), p.note(), p.createdBy());
    }
}
