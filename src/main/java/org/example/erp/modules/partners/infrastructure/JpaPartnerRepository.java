package org.example.erp.modules.partners.infrastructure;

import org.example.erp.modules.partners.domain.Partner;
import org.example.erp.modules.partners.domain.PartnerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaPartnerRepository implements PartnerRepository {
    private final PartnerJpaRepository jpa;

    JpaPartnerRepository(PartnerJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Partner> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Partner> findAllByName() {
        return jpa.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public Partner save(Partner p) {
        PartnerRecord r = p.getId() == null ? new PartnerRecord() : jpa.getReferenceById(p.getId());
        r.setName(p.getName());
        r.setType(p.getType());
        r.setContactPerson(p.getContactPerson());
        r.setPhone(p.getPhone());
        r.setEmail(p.getEmail());
        r.setAddress(p.getAddress());
        r.setTaxId(p.getTaxId());
        r.setActive(p.isActive());
        return toDomain(jpa.save(r));
    }

    private Partner toDomain(PartnerRecord r) {
        return Partner.restore(r.getId(), r.getName(), r.getType(), r.getContactPerson(), r.getPhone(), r.getEmail(),
                r.getAddress(), r.getTaxId(), r.isActive());
    }
}
