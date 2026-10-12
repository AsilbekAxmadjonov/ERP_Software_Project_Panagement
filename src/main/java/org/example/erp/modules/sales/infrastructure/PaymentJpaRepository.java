package org.example.erp.modules.sales.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PaymentJpaRepository extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findAllByOrderByPaymentDateDescIdDesc();
}
