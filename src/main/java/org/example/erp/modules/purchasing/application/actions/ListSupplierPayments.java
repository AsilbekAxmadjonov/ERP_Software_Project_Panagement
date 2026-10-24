package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.dto.SupplierPaymentDTO;
import org.example.erp.modules.purchasing.domain.SupplierPaymentRepository;

import java.util.List;

public final class ListSupplierPayments {
    private final SupplierPaymentRepository payments;

    public ListSupplierPayments(SupplierPaymentRepository payments) {
        this.payments = payments;
    }

    public List<SupplierPaymentDTO> handle() {
        return payments.findAllNewestFirst().stream().map(PurchasingMapper::toDto).toList();
    }
}
