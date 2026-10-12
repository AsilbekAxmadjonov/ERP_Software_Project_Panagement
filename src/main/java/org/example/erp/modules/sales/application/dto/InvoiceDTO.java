package org.example.erp.modules.sales.application.dto;

import org.example.erp.modules.sales.application.enums.PaymentProgress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDTO(long id, String number, long customerId, String customerName, LocalDate invoiceDate,
                         BigDecimal total, BigDecimal cost, BigDecimal paid, BigDecimal outstanding,
                         PaymentProgress status, String createdBy, List<InvoiceLineDTO> lines) {}
