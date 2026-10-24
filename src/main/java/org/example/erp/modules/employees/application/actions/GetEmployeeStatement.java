package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.dto.EmployeeLedgerRowDTO;
import org.example.erp.modules.employees.application.dto.EmployeeStatementDTO;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeLedger;
import org.example.erp.modules.employees.domain.EmployeeLedgerEntry;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Opening balance, the period's entries and the closing balance; both bounds are optional and inclusive. */
public final class GetEmployeeStatement {
    private final EmployeeRepository employees;
    private final EmployeeLedgerRepository ledger;

    public GetEmployeeStatement(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        this.employees = employees;
        this.ledger = ledger;
    }

    public EmployeeStatementDTO handle(long employeeId, LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) throw new BusinessRuleException("The period ends before it starts");
        Employee employee = employees.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));
        EmployeeLedger account = new EmployeeLedger(employee, ledger.findByEmployee(employeeId));

        BigDecimal opening = from == null ? BigDecimal.ZERO.setScale(2) : account.balanceBefore(from);
        List<EmployeeLedgerEntry> inPeriod = account.entries().stream()
                .filter(e -> (from == null || !e.date().isBefore(from)) && (to == null || !e.date().isAfter(to)))
                .toList();
        List<EmployeeLedgerRowDTO> rows = EmployeesMapper.toRows(inPeriod, opening);
        BigDecimal earned = rows.stream().map(EmployeeLedgerRowDTO::earned).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        BigDecimal settled = rows.stream().map(EmployeeLedgerRowDTO::settled).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        return new EmployeeStatementDTO(EmployeesMapper.toDto(employee, account.balance()), from, to, opening, rows, earned, settled,
                opening.add(earned).subtract(settled));
    }
}
