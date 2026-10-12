package org.example.erp.modules.inventory.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ProductJpaRepository extends JpaRepository<ProductRecord, Long> {
    List<ProductRecord> findAllByOrderByNameAsc();

    boolean existsByCode(String code);
}
