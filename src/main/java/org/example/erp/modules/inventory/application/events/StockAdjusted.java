package org.example.erp.modules.inventory.application.events;

import org.example.erp.modules.inventory.application.enums.StockChange;
import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.util.Map;

public final class StockAdjusted extends BaseIntegrationEvent {
    private final long productId;
    private final String productName;
    private final StockChange change;
    private final BigDecimal value;
    private final String reason;

    public StockAdjusted(long productId, String productName, StockChange change, BigDecimal value, String reason) {
        super(null, null);
        this.productId = productId;
        this.productName = productName;
        this.change = change;
        this.value = value;
        this.reason = reason;
    }

    public long productId() {
        return productId;
    }

    public String productName() {
        return productName;
    }

    public StockChange change() {
        return change;
    }

    public BigDecimal value() {
        return value;
    }

    public String reason() {
        return reason;
    }

    @Override
    public String eventName() {
        return "inventory.stock-adjusted";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("productId", productId, "productName", productName, "change", change.name(),
                "value", value.toPlainString(), "reason", reason);
    }
}
