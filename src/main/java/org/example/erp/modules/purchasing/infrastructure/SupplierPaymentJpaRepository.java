package org.example.erp.modules.purchasing.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SupplierPaymentJpaRepository extends JpaRepository<SupplierPaymentRecord, Long> {
    List<SupplierPaymentRecord> findAllByOrderByPaymentDateDescIdDesc();
}
