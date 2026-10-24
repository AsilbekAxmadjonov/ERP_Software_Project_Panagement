package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.dto.EmployeeLedgerRowDTO;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeLedger;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.platform.NotFoundException;

import java.math.BigDecimal;
import java.util.List;

/** The employee's whole account, oldest first, with a running balance. */
public final class GetEmployeeLedger {
    private final EmployeeRepository employees;
    private final EmployeeLedgerRepository ledger;

    public GetEmployeeLedger(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        this.employees = employees;
        this.ledger = ledger;
    }

    public List<EmployeeLedgerRowDTO> handle(long employeeId) {
        Employee employee = employees.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));
        return EmployeesMapper.toRows(new EmployeeLedger(employee, ledger.findByEmployee(employeeId)).entries(), BigDecimal.ZERO.setScale(2));
    }
}
