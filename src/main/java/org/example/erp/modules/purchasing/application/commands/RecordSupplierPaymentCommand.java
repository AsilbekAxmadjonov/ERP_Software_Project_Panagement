package org.example.erp.modules.purchasing.application.commands;

import org.example.erp.modules.purchasing.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;

/** orderId is null for an advance payment; date is optional. */
public record RecordSupplierPaymentCommand(long supplierId, Long orderId, BigDecimal amount, PaymentChannel method,
                                           LocalDate date, String note) {}
