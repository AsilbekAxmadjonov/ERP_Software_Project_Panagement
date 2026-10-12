package org.example.erp.modules.partners.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PartnerJpaRepository extends JpaRepository<PartnerRecord, Long> {
    List<PartnerRecord> findAllByOrderByNameAsc();
}
