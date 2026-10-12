package org.example.erp.modules.sales.domain;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
    Optional<Invoice> findById(long id);

    List<Invoice> findAllNewestFirst();

    Invoice save(Invoice invoice);
}
