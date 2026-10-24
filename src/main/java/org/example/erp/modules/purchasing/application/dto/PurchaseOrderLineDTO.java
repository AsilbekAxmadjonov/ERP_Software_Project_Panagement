package org.example.erp.modules.purchasing.application.dto;

import java.math.BigDecimal;

public record PurchaseOrderLineDTO(long productId, String productName, String unit, BigDecimal quantity,
                                   BigDecimal unitCost, BigDecimal lineTotal) {}
