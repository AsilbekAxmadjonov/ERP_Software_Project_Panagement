package org.example.erp.modules.purchasing.domain;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository {
    Optional<PurchaseOrder> findById(long id);

    List<PurchaseOrder> findAllNewestFirst();

    PurchaseOrder save(PurchaseOrder order);
}
