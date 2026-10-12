package org.example.erp.modules.partners.application.actions;

import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.modules.partners.domain.PartnerRepository;

import java.util.List;

public final class ListPartners {
    private final PartnerRepository partners;

    public ListPartners(PartnerRepository partners) {
        this.partners = partners;
    }

    public List<PartnerDTO> handle() {
        return partners.findAllByName().stream().map(PartnerMapper::toDto).toList();
    }
}
