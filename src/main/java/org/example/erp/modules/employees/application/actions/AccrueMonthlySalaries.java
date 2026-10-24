package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.commands.RecordLedgerEntryCommand;
import org.example.erp.modules.employees.application.dto.EmployeeLedgerRowDTO;
import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.modules.employees.domain.LedgerEntryType;
import org.example.erp.platform.TransactionRunner;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Payroll run: accrues one month's salary for every active employee with a salary who has not been accrued for that
 * month yet. All accruals are saved together or not at all.
 */
public final class AccrueMonthlySalaries {
    private final EmployeeRepository employees;
    private final EmployeeLedgerRepository ledger;
    private final RecordEmployeeLedgerEntry recordEntry;
    private final TransactionRunner tx;

    public AccrueMonthlySalaries(EmployeeRepository employees, EmployeeLedgerRepository ledger, RecordEmployeeLedgerEntry recordEntry,
                                 TransactionRunner tx) {
        this.employees = employees;
        this.ledger = ledger;
        this.recordEntry = recordEntry;
        this.tx = tx;
    }

    /** date is optional: today is used when absent. */
    public List<EmployeeLedgerRowDTO> handle(YearMonth period, LocalDate date) {
        return tx.run(() -> {
            List<EmployeeLedgerRowDTO> accrued = new ArrayList<>();
            for (Employee employee : employees.findAllByName()) {
                if (!employee.isActive() || employee.getMonthlySalary().signum() == 0) continue;
                boolean done = ledger.findByEmployee(employee.getId()).stream()
                        .anyMatch(e -> e.type() == LedgerEntryType.SALARY_ACCRUED && period.equals(e.period()));
                if (done) continue;
                accrued.add(recordEntry.handle(new RecordLedgerEntryCommand(employee.getId(), LedgerEntryKind.SALARY_ACCRUED, date,
                        null, null, period, "Salary " + period)));
            }
            return accrued;
        });
    }
}
