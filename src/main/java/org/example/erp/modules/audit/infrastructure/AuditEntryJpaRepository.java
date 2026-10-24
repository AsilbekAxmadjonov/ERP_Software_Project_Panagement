package org.example.erp.modules.audit.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface AuditEntryJpaRepository extends JpaRepository<AuditEntryRecord, Long>, JpaSpecificationExecutor<AuditEntryRecord> {}
