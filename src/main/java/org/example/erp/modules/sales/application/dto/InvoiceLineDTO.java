package org.example.erp.modules.sales.application.dto;

import java.math.BigDecimal;

/** returnable = quantity - returnedQuantity, the most a credit note can still take back. */
public record InvoiceLineDTO(long productId, String productName, String unit, BigDecimal quantity, BigDecimal unitPrice,
                             BigDecimal lineTotal, BigDecimal returnedQuantity, BigDecimal returnable) {}
