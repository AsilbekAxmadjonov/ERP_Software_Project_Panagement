package org.example.erp.modules.partners.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.partners.domain.PartnerType;

@Entity
@Table(name = "partners_partners")
@Getter @Setter
class PartnerRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PartnerType type;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String taxId;
    private boolean active;
}
