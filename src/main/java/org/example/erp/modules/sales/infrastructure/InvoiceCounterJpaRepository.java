package org.example.erp.modules.sales.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface InvoiceCounterJpaRepository extends JpaRepository<InvoiceCounterRecord, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from InvoiceCounterRecord c where c.id = 1")
    Optional<InvoiceCounterRecord> findForUpdate();
}
