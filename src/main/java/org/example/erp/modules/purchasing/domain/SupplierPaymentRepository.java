package org.example.erp.modules.purchasing.domain;

import java.util.List;

public interface SupplierPaymentRepository {
    SupplierPayment save(SupplierPayment payment);

    List<SupplierPayment> findAllNewestFirst();
}
