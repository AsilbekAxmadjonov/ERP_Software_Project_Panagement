package org.example.erp.modules.employees.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.employees.domain.EmployeeStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "employees_staff")
@Getter @Setter
class EmployeeRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;
    private String position;
    private String phone;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal monthlySalary;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private EmployeeStatus status;
}
