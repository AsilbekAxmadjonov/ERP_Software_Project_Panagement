package org.example.erp.modules.employees.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface EmployeeLedgerEntryJpaRepository extends JpaRepository<EmployeeLedgerEntryRecord, Long> {
    List<EmployeeLedgerEntryRecord> findByEmployeeIdOrderByEntryDateAscIdAsc(long employeeId);

    /** Rows of [employeeId, type, sum of amount]. */
    @Query("select e.employeeId, e.type, sum(e.amount) from EmployeeLedgerEntryRecord e group by e.employeeId, e.type")
    List<Object[]> totalsByEmployeeAndType();
}
