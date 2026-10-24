package org.example.erp.modules.sales.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface CreditNoteJpaRepository extends JpaRepository<CreditNoteRecord, Long> {
    List<CreditNoteRecord> findAllByOrderByCreditDateDescIdDesc();

    List<CreditNoteRecord> findByInvoiceIdOrderByIdDesc(long invoiceId);
}
