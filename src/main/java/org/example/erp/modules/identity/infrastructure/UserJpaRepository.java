package org.example.erp.modules.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserJpaRepository extends JpaRepository<UserRecord, Long> {
    Optional<UserRecord> findByUsername(String username);

    boolean existsByUsername(String username);
}
