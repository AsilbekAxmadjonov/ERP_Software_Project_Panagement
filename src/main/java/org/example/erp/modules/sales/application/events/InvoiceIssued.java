package org.example.erp.modules.sales.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public final class InvoiceIssued extends BaseIntegrationEvent {
    private final String invoiceNumber;
    private final long customerId;
    private final String customerName;
    private final LocalDate invoiceDate;
    private final BigDecimal total;
    private final BigDecimal cost;

    public InvoiceIssued(String invoiceNumber, long customerId, String customerName, LocalDate invoiceDate,
                         BigDecimal total, BigDecimal cost) {
        super(invoiceNumber, null);
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.total = total;
        this.cost = cost;
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

    public LocalDate invoiceDate() {
        return invoiceDate;
    }

    public BigDecimal total() {
        return total;
    }

    public BigDecimal cost() {
        return cost;
    }

    @Override
    public String eventName() {
        return "sales.invoice-issued";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("invoiceNumber", invoiceNumber, "customerId", customerId, "customerName", customerName,
                "invoiceDate", invoiceDate.toString(), "total", total.toPlainString(), "cost", cost.toPlainString());
    }
}
