package org.example.erp.modules.employees.domain;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findById(long id);

    List<Employee> findAllByName();

    Employee save(Employee employee);
}
