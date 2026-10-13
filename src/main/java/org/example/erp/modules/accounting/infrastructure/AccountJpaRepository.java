package org.example.erp.modules.accounting.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface AccountJpaRepository extends JpaRepository<AccountRecord, Long> {
    Optional<AccountRecord> findByCode(String code);

    List<AccountRecord> findAllByOrderByCodeAsc();
}
