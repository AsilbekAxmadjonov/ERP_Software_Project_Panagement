package org.example.erp.modules.employees.infrastructure;

import org.example.erp.modules.employees.application.actions.*;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class EmployeesModuleConfig {

    @Bean
    CreateEmployee createEmployee(EmployeeRepository employees, TransactionRunner tx) {
        return new CreateEmployee(employees, tx);
    }

    @Bean
    UpdateEmployee updateEmployee(EmployeeRepository employees, EmployeeLedgerRepository ledger, TransactionRunner tx) {
        return new UpdateEmployee(employees, ledger, tx);
    }

    @Bean
    ListEmployees listEmployees(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        return new ListEmployees(employees, ledger);
    }

    @Bean
    FindEmployee findEmployee(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        return new FindEmployee(employees, ledger);
    }

    @Bean
    RecordEmployeeLedgerEntry recordEmployeeLedgerEntry(EmployeeRepository employees, EmployeeLedgerRepository ledger,
                                                        IntegrationEventPublisher events, TransactionRunner tx, CurrentActor actor,
                                                        Clock clock) {
        return new RecordEmployeeLedgerEntry(employees, ledger, events, tx, actor, clock);
    }

    @Bean
    AccrueMonthlySalaries accrueMonthlySalaries(EmployeeRepository employees, EmployeeLedgerRepository ledger,
                                                RecordEmployeeLedgerEntry recordEntry, TransactionRunner tx) {
        return new AccrueMonthlySalaries(employees, ledger, recordEntry, tx);
    }

    @Bean
    GetEmployeeLedger getEmployeeLedger(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        return new GetEmployeeLedger(employees, ledger);
    }

    @Bean
    GetEmployeeStatement getEmployeeStatement(EmployeeRepository employees, EmployeeLedgerRepository ledger) {
        return new GetEmployeeStatement(employees, ledger);
    }
}
