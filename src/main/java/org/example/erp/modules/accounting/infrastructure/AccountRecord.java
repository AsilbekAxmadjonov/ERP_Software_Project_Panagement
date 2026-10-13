package org.example.erp.modules.accounting.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.accounting.domain.AccountType;

@Entity
@Table(name = "accounting_accounts")
@Getter @Setter
class AccountRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AccountType type;
    private boolean active;
}
