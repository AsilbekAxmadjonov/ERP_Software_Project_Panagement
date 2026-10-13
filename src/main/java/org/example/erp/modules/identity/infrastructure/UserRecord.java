package org.example.erp.modules.identity.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.identity.domain.Role;

@Entity
@Table(name = "identity_users")
@Getter @Setter
class UserRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    private String fullName;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role;
    private boolean active;
}
