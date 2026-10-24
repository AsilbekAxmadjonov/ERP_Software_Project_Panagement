package org.example.erp.modules.sales.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Goods taken back from a customer against one invoice. Created only through {@link Invoice#creditReturn}. */
@Getter
public final class CreditNote {
    private final Long id;
    private final String number;
    private final long invoiceId;
    private final String invoiceNumber;
    private final long customerId;
    private final String customerName;
    private final LocalDate date;
    private final String reason;
    private final List<CreditNoteLine> lines;
    private final BigDecimal total;
    private final BigDecimal cost;
    private final String createdBy;

    private CreditNote(Long id, String number, long invoiceId, String invoiceNumber, long customerId, String customerName,
                       LocalDate date, String reason, List<CreditNoteLine> lines, String createdBy) {
        this.id = id;
        this.number = number;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.date = date;
        this.reason = reason;
        this.lines = List.copyOf(lines);
        this.total = lines.stream().map(CreditNoteLine::amount).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        this.cost = lines.stream().map(CreditNoteLine::cost).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        this.createdBy = createdBy;
    }

    static CreditNote issue(String number, Invoice invoice, LocalDate date, String reason, List<CreditNoteLine> lines,
                            String createdBy) {
        return new CreditNote(null, number, invoice.getId(), invoice.getNumber(), invoice.getCustomerId(),
                invoice.getCustomerName(), date, reason, lines, createdBy);
    }

    public static CreditNote restore(Long id, String number, long invoiceId, String invoiceNumber, long customerId,
                                     String customerName, LocalDate date, String reason, List<CreditNoteLine> lines,
                                     String createdBy) {
        return new CreditNote(id, number, invoiceId, invoiceNumber, customerId, customerName, date, reason, lines, createdBy);
    }
}
