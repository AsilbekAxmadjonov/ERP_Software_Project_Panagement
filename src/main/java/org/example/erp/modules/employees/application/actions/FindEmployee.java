package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.dto.EmployeeDTO;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeLedger;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.platform.NotFoundException;

public final class FindEmployee {
    private final EmployeeRepository employees;
    private final EmployeeLedgerRepository ledger;

    public FindEmployee(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        this.employees = employees;
        this.ledger = ledger;
    }

    public EmployeeDTO handle(long id) {
        Employee employee = employees.findById(id).orElseThrow(() -> new NotFoundException("Employee not found"));
        return EmployeesMapper.toDto(employee, new EmployeeLedger(employee, ledger.findByEmployee(id)).balance());
    }
}
