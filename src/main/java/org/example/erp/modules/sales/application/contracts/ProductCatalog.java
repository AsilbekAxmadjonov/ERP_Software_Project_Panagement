package org.example.erp.modules.sales.application.contracts;

import java.math.BigDecimal;
import java.util.Optional;

/** Host-supplied port: what can be sold. Implemented in the composition layer. */
public interface ProductCatalog {
    record ProductInfo(long id, String name, String unit, BigDecimal salePrice, boolean active) {}

    Optional<ProductInfo> find(long productId);
}
