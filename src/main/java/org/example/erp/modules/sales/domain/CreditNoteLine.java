package org.example.erp.modules.sales.domain;

import java.math.BigDecimal;

/** amount and cost are what this return takes off the invoice's revenue and cost, already rounded to cents. */
public record CreditNoteLine(long productId, String productName, String unit, BigDecimal quantity, BigDecimal unitPrice,
                             BigDecimal unitCost, BigDecimal amount, BigDecimal cost) {}
