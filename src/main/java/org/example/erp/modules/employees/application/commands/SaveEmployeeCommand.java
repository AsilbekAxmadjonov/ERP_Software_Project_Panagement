package org.example.erp.modules.employees.application.commands;

import org.example.erp.modules.employees.application.enums.EmployeeState;

import java.math.BigDecimal;

/** status is optional on creation (new employees are active). */
public record SaveEmployeeCommand(String fullName, String position, String phone, BigDecimal monthlySalary, EmployeeState status) {}
