package org.example.erp.modules.sales.application.commands;

import org.example.erp.modules.sales.application.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;

/** invoiceId is null for an advance payment; date is optional. */
public record RecordPaymentCommand(long customerId, Long invoiceId, BigDecimal amount, PaymentChannel method,
                                   LocalDate date, String note) {}
