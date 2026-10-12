package org.example.erp.modules.partners.application.actions;

import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.modules.partners.domain.PartnerRepository;
import org.example.erp.platform.NotFoundException;

public final class FindPartner {
    private final PartnerRepository partners;

    public FindPartner(PartnerRepository partners) {
        this.partners = partners;
    }

    public PartnerDTO handle(long id) {
        return partners.findById(id).map(PartnerMapper::toDto).orElseThrow(() -> new NotFoundException("Partner not found"));
    }
}
