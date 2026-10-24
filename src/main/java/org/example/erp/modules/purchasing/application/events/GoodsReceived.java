package org.example.erp.modules.purchasing.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** total is what the supplier is now owed and the value added to stock. */
public final class GoodsReceived extends BaseIntegrationEvent {
    private final String orderNumber;
    private final long supplierId;
    private final String supplierName;
    private final LocalDate receivedDate;
    private final BigDecimal total;

    public GoodsReceived(String orderNumber, long supplierId, String supplierName, LocalDate receivedDate, BigDecimal total) {
        super(orderNumber, null);
        this.orderNumber = orderNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.receivedDate = receivedDate;
        this.total = total;
    }

    public String orderNumber() {
        return orderNumber;
    }

    public long supplierId() {
        return supplierId;
    }

    public String supplierName() {
        return supplierName;
    }

    public LocalDate receivedDate() {
        return receivedDate;
    }

    public BigDecimal total() {
        return total;
    }

    @Override
    public String eventName() {
        return "purchasing.goods-received";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("orderNumber", orderNumber, "supplierId", supplierId, "supplierName", supplierName,
                "receivedDate", receivedDate.toString(), "total", total.toPlainString());
    }
}
