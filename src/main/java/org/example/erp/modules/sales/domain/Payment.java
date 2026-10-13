package org.example.erp.modules.sales.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Money received from a customer. An advance has no invoice. */
public record Payment(Long id, long customerId, String customerName, Long invoiceId, String invoiceNumber, LocalDate date,
                      BigDecimal amount, PaymentMethod method, String note, String createdBy) {

    public Payment {
        if (amount.signum() <= 0) throw new BusinessRuleException("Amount must be greater than zero");
    }
}
