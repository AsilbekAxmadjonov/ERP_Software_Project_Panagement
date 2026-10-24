package org.example.erp.modules.employees.infrastructure;

import org.example.erp.modules.employees.domain.EmployeeLedgerEntry;
import org.example.erp.modules.employees.domain.EmployeeLedgerRepository;
import org.example.erp.modules.employees.domain.LedgerEntryType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
class JpaEmployeeLedgerRepository implements EmployeeLedgerRepository {
    private final EmployeeLedgerEntryJpaRepository jpa;

    JpaEmployeeLedgerRepository(EmployeeLedgerEntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<EmployeeLedgerEntry> findByEmployee(long employeeId) {
        return jpa.findByEmployeeIdOrderByEntryDateAscIdAsc(employeeId).stream().map(this::toDomain).toList();
    }

    @Override
    public Map<Long, BigDecimal> balances() {
        Map<Long, BigDecimal> balances = new HashMap<>();
        for (Object[] row : jpa.totalsByEmployeeAndType()) {
            LedgerEntryType type = (LedgerEntryType) row[1];
            BigDecimal total = (BigDecimal) row[2];
            balances.merge((Long) row[0], type.sign() > 0 ? total : total.negate(), BigDecimal::add);
        }
        return balances;
    }

    @Override
    public EmployeeLedgerEntry save(EmployeeLedgerEntry e) {
        EmployeeLedgerEntryRecord r = new EmployeeLedgerEntryRecord();
        r.setEmployeeId(e.employeeId());
        r.setEntryDate(e.date());
        r.setType(e.type());
        r.setAmount(e.amount());
        r.setMethod(e.method());
        r.setPeriod(e.period() == null ? null : e.period().toString());
        r.setDescription(e.description());
        r.setCreatedBy(e.createdBy());
        return toDomain(jpa.save(r));
    }

    private EmployeeLedgerEntry toDomain(EmployeeLedgerEntryRecord r) {
        return new EmployeeLedgerEntry(r.getId(), r.getEmployeeId(), r.getEntryDate(), r.getType(), r.getAmount(), r.getMethod(),
                r.getPeriod() == null ? null : YearMonth.parse(r.getPeriod()), r.getDescription(), r.getCreatedBy());
    }
}
