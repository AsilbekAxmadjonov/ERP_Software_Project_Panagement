package org.example.erp.modules.purchasing.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderRecord, Long> {
    List<PurchaseOrderRecord> findAllByOrderByOrderDateDescIdDesc();
}
