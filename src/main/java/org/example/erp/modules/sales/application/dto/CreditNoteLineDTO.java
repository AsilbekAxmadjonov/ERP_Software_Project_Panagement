package org.example.erp.modules.sales.application.dto;

import java.math.BigDecimal;

public record CreditNoteLineDTO(long productId, String productName, String unit, BigDecimal quantity, BigDecimal unitPrice,
                                BigDecimal lineTotal) {}
