package org.example.erp.modules.purchasing.application.contracts;

import java.util.Optional;

/** Host-supplied port: who the suppliers are. Implemented in the composition layer. */
public interface SupplierDirectory {
    record SupplierInfo(long id, String name, boolean supplier, boolean active) {}

    Optional<SupplierInfo> find(long supplierId);
}
