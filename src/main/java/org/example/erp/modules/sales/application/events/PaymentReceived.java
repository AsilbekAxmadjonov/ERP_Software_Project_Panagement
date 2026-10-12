package org.example.erp.modules.sales.application.events;

import org.example.erp.modules.sales.application.enums.PaymentChannel;
import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class PaymentReceived extends BaseIntegrationEvent {
    private final String reference;
    private final long customerId;
    private final String customerName;
    private final LocalDate paymentDate;
    private final BigDecimal amount;
    private final PaymentChannel channel;
    private final String invoiceNumber;

    /** invoiceNumber is null for an advance payment. */
    public PaymentReceived(String reference, long customerId, String customerName, LocalDate paymentDate,
                           BigDecimal amount, PaymentChannel channel, String invoiceNumber) {
        super(invoiceNumber, null);
        this.reference = reference;
        this.customerId = customerId;
        this.customerName = customerName;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.channel = channel;
        this.invoiceNumber = invoiceNumber;
    }

    public String reference() {
        return reference;
    }

    public long customerId() {
        return customerId;
    }

    public String customerName() {
        return customerName;
    }

    public LocalDate paymentDate() {
        return paymentDate;
    }

    public BigDecimal amount() {
        return amount;
    }

    public PaymentChannel channel() {
        return channel;
    }

    @Override
    public String eventName() {
        return "sales.payment-received";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reference", reference);
        payload.put("customerId", customerId);
        payload.put("customerName", customerName);
        payload.put("paymentDate", paymentDate.toString());
        payload.put("amount", amount.toPlainString());
        payload.put("channel", channel.name());
        payload.put("invoiceNumber", invoiceNumber);
        return payload;
    }
}
