package org.example.erp.modules.partners.application.actions;

import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.modules.partners.domain.Partner;
import org.example.erp.modules.partners.domain.PartnerRepository;

public final class CreatePartner {
    private final PartnerRepository partners;

    public CreatePartner(PartnerRepository partners) {
        this.partners = partners;
    }

    public PartnerDTO handle(SavePartnerCommand c) {
        Partner partner = Partner.register(c.name().trim(), PartnerMapper.toDomain(c.type()), c.contactPerson(),
                c.phone(), c.email(), c.address(), c.taxId());
        return PartnerMapper.toDto(partners.save(partner));
    }
}
