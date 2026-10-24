package org.example.erp.modules.employees.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;

@Getter
public final class Employee {
    private final Long id;
    private String fullName;
    private String position;
    private String phone;
    private BigDecimal monthlySalary;
    private EmployeeStatus status;

    private Employee(Long id, String fullName, String position, String phone, BigDecimal monthlySalary, EmployeeStatus status) {
        this.id = id;
        this.status = status;
        update(fullName, position, phone, monthlySalary);
    }

    public static Employee hire(String fullName, String position, String phone, BigDecimal monthlySalary) {
        return new Employee(null, fullName, position, phone, monthlySalary, EmployeeStatus.ACTIVE);
    }

    public static Employee restore(Long id, String fullName, String position, String phone, BigDecimal monthlySalary,
                                   EmployeeStatus status) {
        return new Employee(id, fullName, position, phone, monthlySalary, status);
    }

    public void update(String fullName, String position, String phone, BigDecimal monthlySalary) {
        if (fullName == null || fullName.isBlank()) throw new BusinessRuleException("Employee name is required");
        if (monthlySalary.signum() < 0) throw new BusinessRuleException("Monthly salary cannot be negative");
        this.fullName = fullName.trim();
        this.position = blankToNull(position);
        this.phone = blankToNull(phone);
        this.monthlySalary = monthlySalary;
    }

    public void activate() {
        this.status = EmployeeStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = EmployeeStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
