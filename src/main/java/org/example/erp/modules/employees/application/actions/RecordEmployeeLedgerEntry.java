package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.commands.RecordLedgerEntryCommand;
import org.example.erp.modules.employees.application.dto.EmployeeLedgerRowDTO;
import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.application.events.EmployeeLedgerEntryRecorded;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeLedger;
import org.example.erp.modules.employees.domain.EmployeeLedgerEntry;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Records salary, a bonus, a payment, an advance or a deduction on an employee's account. The general-ledger entry is
 * posted in the same transaction by the composition listener, so a closed period rejects both.
 */
public final class RecordEmployeeLedgerEntry {
    private final EmployeeRepository employees;
    private final EmployeeLedgerRepository ledger;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public RecordEmployeeLedgerEntry(EmployeeRepository employees, EmployeeLedgerRepository ledger, IntegrationEventPublisher events,
                                     TransactionRunner tx, CurrentActor actor, Clock clock) {
        this.employees = employees;
        this.ledger = ledger;
        this.events = events;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public EmployeeLedgerRowDTO handle(RecordLedgerEntryCommand c) {
        LocalDate date = c.date() == null ? LocalDate.now(clock) : c.date();
        boolean accrual = c.type() == LedgerEntryKind.SALARY_ACCRUED;
        YearMonth period = accrual ? (c.period() == null ? YearMonth.from(date) : c.period()) : null;
        return tx.run(() -> {
            Employee employee = employees.findById(c.employeeId()).orElseThrow(() -> new NotFoundException("Employee not found"));
            BigDecimal amount = c.amount() != null ? c.amount() : accrual ? employee.getMonthlySalary() : null;
            if (amount == null) throw new BusinessRuleException("Amount is required");

            EmployeeLedger account = new EmployeeLedger(employee, ledger.findByEmployee(employee.getId()));
            EmployeeLedgerEntry entry = account.record(EmployeesMapper.toType(c.type()), date, amount.setScale(2, RoundingMode.HALF_UP),
                    EmployeesMapper.toMethod(c.method()), period, c.description(), actor.username());
            EmployeeLedgerEntry saved = ledger.save(entry);
            events.publish(new EmployeeLedgerEntryRecorded(saved.reference(), employee.getId(), employee.getFullName(), saved.date(),
                    c.type(), saved.amount(), c.method()));
            return EmployeesMapper.toRow(saved, account.balance().add(saved.signedAmount()));
        });
    }
}
