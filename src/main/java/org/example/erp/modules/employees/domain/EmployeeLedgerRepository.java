package org.example.erp.modules.employees.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EmployeeLedgerRepository {
    List<EmployeeLedgerEntry> findByEmployee(long employeeId);

    /** Current balance per employee id; employees without entries are absent. */
    Map<Long, BigDecimal> balances();

    EmployeeLedgerEntry save(EmployeeLedgerEntry entry);
}
