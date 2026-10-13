package org.example.erp.modules.sales.application.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Host-supplied port: takes goods out of stock and reports the unit cost they were valued at. */
public interface StockIssuer {
    BigDecimal issue(long productId, BigDecimal quantity, String reference, LocalDate date);
}
