package org.example.erp.modules.employees.application.actions;

import org.example.erp.modules.employees.application.dto.EmployeeDTO;
import org.example.erp.modules.employees.application.dto.EmployeeLedgerRowDTO;
import org.example.erp.modules.employees.application.enums.EmployeeState;
import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.application.enums.PaymentChannel;
import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeLedgerEntry;
import org.example.erp.modules.employees.domain.LedgerEntryType;
import org.example.erp.modules.employees.domain.PaymentMethod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class EmployeesMapper {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private EmployeesMapper() {}

    static EmployeeDTO toDto(Employee e, BigDecimal balance) {
        return new EmployeeDTO(e.getId(), e.getFullName(), e.getPosition(), e.getPhone(), e.getMonthlySalary(),
                EmployeeState.valueOf(e.getStatus().name()), balance);
    }

    static LedgerEntryType toType(LedgerEntryKind kind) {
        return LedgerEntryType.valueOf(kind.name());
    }

    static PaymentMethod toMethod(PaymentChannel channel) {
        return channel == null ? null : PaymentMethod.valueOf(channel.name());
    }

    static PaymentChannel toChannel(PaymentMethod method) {
        return method == null ? null : PaymentChannel.valueOf(method.name());
    }

    static EmployeeLedgerRowDTO toRow(EmployeeLedgerEntry e, BigDecimal balanceAfter) {
        boolean earned = e.type().sign() > 0;
        return new EmployeeLedgerRowDTO(e.id(), e.reference(), e.employeeId(), e.date(), LedgerEntryKind.valueOf(e.type().name()),
                e.period() == null ? null : e.period().toString(), toChannel(e.method()), e.description(),
                earned ? e.amount() : ZERO, earned ? ZERO : e.amount(), balanceAfter, e.createdBy());
    }

    /** Rows for chronologically ordered entries, with the running balance starting from opening. */
    static List<EmployeeLedgerRowDTO> toRows(List<EmployeeLedgerEntry> entries, BigDecimal opening) {
        List<EmployeeLedgerRowDTO> rows = new ArrayList<>();
        BigDecimal balance = opening;
        for (EmployeeLedgerEntry e : entries) {
            balance = balance.add(e.signedAmount());
            rows.add(toRow(e, balance));
        }
        return rows;
    }
}
