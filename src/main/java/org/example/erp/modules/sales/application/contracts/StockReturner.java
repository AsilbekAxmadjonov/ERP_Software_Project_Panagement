package org.example.erp.modules.sales.application.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Host-supplied port: puts returned goods back into stock at the unit cost they were sold at. */
public interface StockReturner {
    void returnToStock(long productId, BigDecimal quantity, BigDecimal unitCost, String reference, LocalDate date);
}
