package org.example.erp.modules.accounting.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface PeriodLockJpaRepository extends JpaRepository<PeriodLockRecord, Long> {}
