package org.example.erp.modules.sales.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public final class Invoice {
    private final Long id;
    private final String number;
    private final long customerId;
    private final String customerName;
    private final LocalDate invoiceDate;
    private final List<InvoiceLine> lines;
    private final BigDecimal total;
    private final BigDecimal cost;
    private BigDecimal paid;
    private final String createdBy;

    private Invoice(Long id, String number, long customerId, String customerName, LocalDate invoiceDate,
                    List<InvoiceLine> lines, BigDecimal paid, String createdBy) {
        this.id = id;
        this.number = number;
        this.customerId = customerId;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.lines = List.copyOf(lines);
        this.total = lines.stream().map(InvoiceLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.cost = lines.stream().map(InvoiceLine::lineCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.paid = paid;
        this.createdBy = createdBy;
    }

    public static Invoice issue(String number, long customerId, String customerName, LocalDate date,
                                List<InvoiceLine> lines, String createdBy) {
        if (lines.isEmpty()) throw new BusinessRuleException("Add at least one item");
        Invoice invoice = new Invoice(null, number, customerId, customerName, date, lines, BigDecimal.ZERO.setScale(2), createdBy);
        if (invoice.total.signum() <= 0) throw new BusinessRuleException("Invoice total must be greater than zero");
        return invoice;
    }

    public static Invoice restore(Long id, String number, long customerId, String customerName, LocalDate date,
                                  List<InvoiceLine> lines, BigDecimal paid, String createdBy) {
        return new Invoice(id, number, customerId, customerName, date, lines, paid, createdBy);
    }

    public BigDecimal outstanding() {
        return total.subtract(paid);
    }

    public void applyPayment(BigDecimal amount) {
        if (amount.compareTo(outstanding()) > 0)
            throw new BusinessRuleException("Payment exceeds the outstanding amount of " + outstanding());
        this.paid = paid.add(amount);
    }
}
