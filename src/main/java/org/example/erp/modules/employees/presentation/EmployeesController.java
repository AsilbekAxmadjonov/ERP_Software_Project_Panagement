package org.example.erp.modules.employees.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.erp.modules.employees.application.actions.*;
import org.example.erp.modules.employees.application.commands.RecordLedgerEntryCommand;
import org.example.erp.modules.employees.application.commands.SaveEmployeeCommand;
import org.example.erp.modules.employees.application.dto.EmployeeDTO;
import org.example.erp.modules.employees.application.dto.EmployeeLedgerRowDTO;
import org.example.erp.modules.employees.application.dto.EmployeeStatementDTO;
import org.example.erp.modules.employees.application.enums.EmployeeState;
import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.application.enums.PaymentChannel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
class EmployeesController {
    private final ListEmployees listEmployees;
    private final FindEmployee findEmployee;
    private final CreateEmployee createEmployee;
    private final UpdateEmployee updateEmployee;
    private final GetEmployeeLedger getLedger;
    private final RecordEmployeeLedgerEntry recordEntry;
    private final GetEmployeeStatement getStatement;
    private final AccrueMonthlySalaries accrueSalaries;

    EmployeesController(ListEmployees listEmployees, FindEmployee findEmployee, CreateEmployee createEmployee,
                        UpdateEmployee updateEmployee, GetEmployeeLedger getLedger, RecordEmployeeLedgerEntry recordEntry,
                        GetEmployeeStatement getStatement, AccrueMonthlySalaries accrueSalaries) {
        this.listEmployees = listEmployees;
        this.findEmployee = findEmployee;
        this.createEmployee = createEmployee;
        this.updateEmployee = updateEmployee;
        this.getLedger = getLedger;
        this.recordEntry = recordEntry;
        this.getStatement = getStatement;
        this.accrueSalaries = accrueSalaries;
    }

    record EmployeeRequest(@NotBlank String fullName, String position, String phone, @NotNull BigDecimal monthlySalary,
                           EmployeeState status) {}

    /** period is yyyy-MM. */
    record EntryRequest(@NotNull LedgerEntryKind type, LocalDate date, BigDecimal amount, PaymentChannel method,
                        YearMonth period, String description) {}

    record AccrualRequest(@NotNull YearMonth period, LocalDate date) {}

    @GetMapping
    List<EmployeeDTO> employees() {
        return listEmployees.handle();
    }

    @PostMapping
    EmployeeDTO create(@RequestBody @Valid EmployeeRequest r) {
        return createEmployee.handle(new SaveEmployeeCommand(r.fullName(), r.position(), r.phone(), r.monthlySalary(), r.status()));
    }

    @GetMapping("/{id}")
    EmployeeDTO employee(@PathVariable long id) {
        return findEmployee.handle(id);
    }

    @PutMapping("/{id}")
    EmployeeDTO update(@PathVariable long id, @RequestBody @Valid EmployeeRequest r) {
        return updateEmployee.handle(id, new SaveEmployeeCommand(r.fullName(), r.position(), r.phone(), r.monthlySalary(), r.status()));
    }

    @GetMapping("/{id}/ledger")
    List<EmployeeLedgerRowDTO> ledger(@PathVariable long id) {
        return getLedger.handle(id);
    }

    @PostMapping("/{id}/ledger")
    EmployeeLedgerRowDTO record(@PathVariable long id, @RequestBody @Valid EntryRequest r) {
        return recordEntry.handle(new RecordLedgerEntryCommand(id, r.type(), r.date(), r.amount(), r.method(), r.period(), r.description()));
    }

    @GetMapping("/{id}/statement")
    EmployeeStatementDTO statement(@PathVariable long id,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return getStatement.handle(id, from, to);
    }

    /** Accrues the month's salary for every active employee not yet accrued for it; returns the new entries. */
    @PostMapping("/payroll/accrue")
    List<EmployeeLedgerRowDTO> accrue(@RequestBody @Valid AccrualRequest r) {
        return accrueSalaries.handle(r.period(), r.date());
    }
}
