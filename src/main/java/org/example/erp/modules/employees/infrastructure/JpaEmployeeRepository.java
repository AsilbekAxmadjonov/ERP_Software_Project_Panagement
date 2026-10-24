package org.example.erp.modules.employees.infrastructure;

import org.example.erp.modules.employees.domain.Employee;
import org.example.erp.modules.employees.domain.EmployeeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaEmployeeRepository implements EmployeeRepository {
    private final EmployeeJpaRepository jpa;

    JpaEmployeeRepository(EmployeeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Employee> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Employee> findAllByName() {
        return jpa.findAllByOrderByFullNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public Employee save(Employee e) {
        EmployeeRecord r = e.getId() == null ? new EmployeeRecord() : jpa.getReferenceById(e.getId());
        r.setFullName(e.getFullName());
        r.setPosition(e.getPosition());
        r.setPhone(e.getPhone());
        r.setMonthlySalary(e.getMonthlySalary());
        r.setStatus(e.getStatus());
        return toDomain(jpa.save(r));
    }

    private Employee toDomain(EmployeeRecord r) {
        return Employee.restore(r.getId(), r.getFullName(), r.getPosition(), r.getPhone(), r.getMonthlySalary(), r.getStatus());
    }
}
