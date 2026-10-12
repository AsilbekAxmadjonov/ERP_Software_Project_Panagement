package org.example.erp.modules.sales.application.contracts;

import java.util.Optional;

/** Host-supplied port: who the customers are. Implemented in the composition layer. */
public interface CustomerDirectory {
    record CustomerInfo(long id, String name, boolean customer, boolean active) {}

    Optional<CustomerInfo> find(long customerId);
}
