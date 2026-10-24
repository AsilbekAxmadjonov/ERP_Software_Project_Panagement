package org.example.erp.modules.purchasing.application.dto;

import org.example.erp.modules.purchasing.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SupplierPaymentDTO(long id, long supplierId, String supplierName, Long orderId, String orderNumber,
                                 LocalDate date, BigDecimal amount, PaymentChannel method, String note, String createdBy) {}
