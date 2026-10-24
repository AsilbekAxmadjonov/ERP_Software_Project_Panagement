package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.dto.EmployeeDTO;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class ListEmployees {
    private final EmployeeRepository employees;
    private final EmployeeLedgerRepository ledger;

    public ListEmployees(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        this.employees = employees;
        this.ledger = ledger;
    }

    public List<EmployeeDTO> handle() {
        Map<Long, BigDecimal> balances = ledger.balances();
        return employees.findAllByName().stream()
                .map(e -> EmployeesMapper.toDto(e, balances.getOrDefault(e.getId(), BigDecimal.ZERO.setScale(2))))
                .toList();
    }
}
