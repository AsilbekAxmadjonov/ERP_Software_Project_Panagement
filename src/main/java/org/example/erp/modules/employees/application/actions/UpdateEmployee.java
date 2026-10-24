package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.commands.SaveEmployeeCommand;
import org.example.erp.modules.employees.application.dto.EmployeeDTO;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeLedger;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.RoundingMode;

public final class UpdateEmployee {
    private final EmployeeRepository employees;
    private final EmployeeLedgerRepository ledger;
    private final TransactionRunner tx;

    public UpdateEmployee(EmployeeRepository employees, EmployeeLedgerRepository ledger, TransactionRunner tx) {
        this.employees = employees;
        this.ledger = ledger;
        this.tx = tx;
    }

    public EmployeeDTO handle(long id, SaveEmployeeCommand c) {
        return tx.run(() -> {
            Employee employee = employees.findById(id).orElseThrow(() -> new NotFoundException("Employee not found"));
            employee.update(c.fullName(), c.position(), c.phone(), c.monthlySalary().setScale(2, RoundingMode.HALF_UP));
            if (c.status() != null) {
                switch (c.status()) {
                    case ACTIVE -> employee.activate();
                    case INACTIVE -> employee.deactivate();
                }
            }
            Employee saved = employees.save(employee);
            return EmployeesMapper.toDto(saved, new EmployeeLedger(saved, ledger.findByEmployee(id)).balance());
        });
    }
}
