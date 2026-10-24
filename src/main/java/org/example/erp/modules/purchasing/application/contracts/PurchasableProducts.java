package org.example.erp.modules.purchasing.application.contracts;

import java.util.Optional;

/** Host-supplied port: what can be bought into stock. Implemented in the composition layer. */
public interface PurchasableProducts {
    record ProductInfo(long id, String name, String unit, boolean active) {}

    Optional<ProductInfo> find(long productId);
}
