package org.example.erp.modules.sales.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesReportRowDTO(LocalDate date, String invoice, long customerId, String customer, long productId,
                                String product, String unit, BigDecimal quantity, BigDecimal unitPrice, BigDecimal total) {}
