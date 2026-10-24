package org.example.erp.modules.purchasing.application.events;

import org.example.erp.modules.purchasing.application.enums.PaymentChannel;
import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class SupplierPaid extends BaseIntegrationEvent {
    private final String reference;
    private final long supplierId;
    private final String supplierName;
    private final LocalDate paymentDate;
    private final BigDecimal amount;
    private final PaymentChannel channel;
    private final String orderNumber;

    /** orderNumber is null for an advance payment. */
    public SupplierPaid(String reference, long supplierId, String supplierName, LocalDate paymentDate, BigDecimal amount,
                        PaymentChannel channel, String orderNumber) {
        super(orderNumber, null);
        this.reference = reference;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.channel = channel;
        this.orderNumber = orderNumber;
    }

    public String reference() {
        return reference;
    }

    public long supplierId() {
        return supplierId;
    }

    public String supplierName() {
        return supplierName;
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
        return "purchasing.supplier-paid";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reference", reference);
        payload.put("supplierId", supplierId);
        payload.put("supplierName", supplierName);
        payload.put("paymentDate", paymentDate.toString());
        payload.put("amount", amount.toPlainString());
        payload.put("channel", channel.name());
        payload.put("orderNumber", orderNumber);
        return payload;
    }
}
