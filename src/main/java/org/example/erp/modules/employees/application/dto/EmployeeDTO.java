package org.example.erp.modules.employees.application.dto;

import org.example.erp.modules.employees.application.enums.EmployeeState;

import java.math.BigDecimal;

/** balance is what the company currently owes the employee; negative when the employee owes (an unearned advance). */
public record EmployeeDTO(long id, String fullName, String position, String phone, BigDecimal monthlySalary,
                          EmployeeState status, BigDecimal balance) {}
