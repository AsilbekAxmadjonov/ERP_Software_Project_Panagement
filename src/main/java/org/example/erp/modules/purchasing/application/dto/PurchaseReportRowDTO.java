package org.example.erp.modules.purchasing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One received order item; date is the receipt date, when the purchase hits stock and payables. */
public record PurchaseReportRowDTO(LocalDate date, long orderId, String order, long supplierId, String supplier, long productId,
                                   String product, String unit, BigDecimal quantity, BigDecimal unitCost, BigDecimal total) {}
