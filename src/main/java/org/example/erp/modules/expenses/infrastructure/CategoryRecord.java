package org.example.erp.modules.expenses.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.expenses.domain.CategoryKind;

@Entity
@Table(name = "expenses_categories")
@Getter @Setter
class CategoryRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private CategoryKind kind;
    @Column(nullable = false)
    private String accountCode;
    private boolean active;
}
