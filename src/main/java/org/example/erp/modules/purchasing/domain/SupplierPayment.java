package org.example.erp.modules.purchasing.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Money paid to a supplier. An advance has no purchase order. */
public record SupplierPayment(Long id, long supplierId, String supplierName, Long orderId, String orderNumber, LocalDate date,
                              BigDecimal amount, PaymentMethod method, String note, String createdBy) {

    public SupplierPayment {
        if (amount.signum() <= 0) throw new BusinessRuleException("Amount must be greater than zero");
    }
}
