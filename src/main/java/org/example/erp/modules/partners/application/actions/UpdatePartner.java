package org.example.erp.modules.partners.application.actions;

import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.modules.partners.application.events.PartnerUpdated;
import org.example.erp.modules.partners.domain.Partner;
import org.example.erp.modules.partners.domain.PartnerRepository;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

public final class UpdatePartner {
    private final PartnerRepository partners;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;

    public UpdatePartner(PartnerRepository partners, IntegrationEventPublisher events, TransactionRunner tx) {
        this.partners = partners;
        this.events = events;
        this.tx = tx;
    }

    public PartnerDTO handle(long id, SavePartnerCommand c) {
        return tx.run(() -> {
            Partner partner = partners.findById(id).orElseThrow(() -> new NotFoundException("Partner not found"));
            partner.update(c.name().trim(), PartnerMapper.toDomain(c.type()), c.contactPerson(), c.phone(), c.email(),
                    c.address(), c.taxId(), c.active());
            PartnerDTO saved = PartnerMapper.toDto(partners.save(partner));
            events.publish(new PartnerUpdated(saved));
            return saved;
        });
    }
}
