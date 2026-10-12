package org.example.erp.modules.sales.application.dto;

import org.example.erp.modules.sales.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentDTO(long id, long customerId, String customerName, Long invoiceId, String invoiceNumber,
                         LocalDate date, BigDecimal amount, PaymentChannel method, String note, String createdBy) {}
