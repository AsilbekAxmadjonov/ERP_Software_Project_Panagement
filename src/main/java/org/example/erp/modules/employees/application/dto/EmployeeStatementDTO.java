package org.example.erp.modules.employees.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** closing = opening + totalEarned - totalSettled; from and to are inclusive and may be open (null). */
public record EmployeeStatementDTO(EmployeeDTO employee, LocalDate from, LocalDate to, BigDecimal opening,
                                   List<EmployeeLedgerRowDTO> rows, BigDecimal totalEarned, BigDecimal totalSettled,
                                   BigDecimal closing) {}
