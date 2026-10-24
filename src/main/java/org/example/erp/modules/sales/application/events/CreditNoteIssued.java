package org.example.erp.modules.sales.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** total is the revenue taken back, cost the cost of the goods returned to stock. */
public final class CreditNoteIssued extends BaseIntegrationEvent {
    private final String creditNoteNumber;
    private final String invoiceNumber;
    private final long customerId;
    private final String customerName;
    private final LocalDate date;
    private final BigDecimal total;
    private final BigDecimal cost;

    public CreditNoteIssued(String creditNoteNumber, String invoiceNumber, long customerId, String customerName,
                            LocalDate date, BigDecimal total, BigDecimal cost) {
        super(invoiceNumber, null);
        this.creditNoteNumber = creditNoteNumber;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.date = date;
        this.total = total;
        this.cost = cost;
    }

    public String creditNoteNumber() {
        return creditNoteNumber;
    }

    public String invoiceNumber() {
        return invoiceNumber;
    }

    public long customerId() {
        return customerId;
    }

    public String customerName() {
        return customerName;
    }

    public LocalDate date() {
        return date;
    }

    public BigDecimal total() {
        return total;
    }

    public BigDecimal cost() {
        return cost;
    }

    @Override
    public String eventName() {
        return "sales.credit-note-issued";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("creditNoteNumber", creditNoteNumber, "invoiceNumber", invoiceNumber, "customerId", customerId,
                "customerName", customerName, "date", date.toString(), "total", total.toPlainString(),
                "cost", cost.toPlainString());
    }
}
