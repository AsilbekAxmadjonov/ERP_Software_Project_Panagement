package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.commands.SaveEmployeeCommand;
import org.example.erp.modules.employees.application.dto.EmployeeDTO;
import org.example.erp.modules.employees.application.enums.EmployeeState;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.platform.TransactionRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CreateEmployee {
    private final EmployeeRepository employees;
    private final TransactionRunner tx;

    public CreateEmployee(EmployeeRepository employees, TransactionRunner tx) {
        this.employees = employees;
        this.tx = tx;
    }

    public EmployeeDTO handle(SaveEmployeeCommand c) {
        return tx.run(() -> {
            Employee employee = Employee.hire(c.fullName(), c.position(), c.phone(), c.monthlySalary().setScale(2, RoundingMode.HALF_UP));
            if (c.status() == EmployeeState.INACTIVE) employee.deactivate();
            return EmployeesMapper.toDto(employees.save(employee), BigDecimal.ZERO.setScale(2));
        });
    }
}
