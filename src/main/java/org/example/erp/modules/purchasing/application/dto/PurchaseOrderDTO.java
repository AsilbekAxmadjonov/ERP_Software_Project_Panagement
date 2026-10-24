package org.example.erp.modules.purchasing.application.dto;

import org.example.erp.modules.purchasing.application.enums.OrderStatus;
import org.example.erp.modules.purchasing.application.enums.PaymentProgress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** billed is the total once received (zero before); outstanding = billed - paid. */
public record PurchaseOrderDTO(long id, String number, long supplierId, String supplierName, LocalDate orderDate,
                               OrderStatus status, LocalDate receivedDate, BigDecimal total, BigDecimal billed,
                               BigDecimal paid, BigDecimal outstanding, PaymentProgress paymentStatus, String createdBy,
                               List<PurchaseOrderLineDTO> lines) {}
