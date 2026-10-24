package org.example.erp.modules.purchasing.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface PurchaseOrderCounterJpaRepository extends JpaRepository<PurchaseOrderCounterRecord, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PurchaseOrderCounterRecord c where c.id = 1")
    Optional<PurchaseOrderCounterRecord> findForUpdate();
}
