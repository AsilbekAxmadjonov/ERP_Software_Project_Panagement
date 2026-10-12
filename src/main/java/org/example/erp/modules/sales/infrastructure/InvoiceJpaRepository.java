package org.example.erp.modules.sales.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface InvoiceJpaRepository extends JpaRepository<InvoiceRecord, Long> {
    List<InvoiceRecord> findAllByOrderByInvoiceDateDescIdDesc();
}
