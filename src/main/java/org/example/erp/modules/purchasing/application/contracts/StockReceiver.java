package org.example.erp.modules.purchasing.application.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Host-supplied port: brings received goods into stock at their purchase cost and returns the value added. */
public interface StockReceiver {
    BigDecimal receive(long productId, BigDecimal quantity, BigDecimal unitCost, String reference, LocalDate date);
}
