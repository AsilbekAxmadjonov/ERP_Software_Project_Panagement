package org.example.erp.modules.employees.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface EmployeeJpaRepository extends JpaRepository<EmployeeRecord, Long> {
    List<EmployeeRecord> findAllByOrderByFullNameAsc();
}
