package org.example.erp.modules.sales.domain;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);

    List<Payment> findAllNewestFirst();
}
