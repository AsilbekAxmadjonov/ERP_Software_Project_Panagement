package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.dto.PaymentDTO;
import org.example.erp.modules.sales.domain.PaymentRepository;

import java.util.List;

public final class ListPayments {
    private final PaymentRepository payments;

    public ListPayments(PaymentRepository payments) {
        this.payments = payments;
    }

    public List<PaymentDTO> handle() {
        return payments.findAllNewestFirst().stream().map(SalesMapper::toDto).toList();
    }
}
