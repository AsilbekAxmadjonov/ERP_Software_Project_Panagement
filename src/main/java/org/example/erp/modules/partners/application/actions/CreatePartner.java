package org.example.erp.modules.partners.application.actions;

import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.modules.partners.application.events.PartnerCreated;
import org.example.erp.modules.partners.domain.Partner;
import org.example.erp.modules.partners.domain.PartnerRepository;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;

public final class CreatePartner {
    private final PartnerRepository partners;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;

    public CreatePartner(PartnerRepository partners, IntegrationEventPublisher events, TransactionRunner tx) {
        this.partners = partners;
        this.events = events;
        this.tx = tx;
    }

    public PartnerDTO handle(SavePartnerCommand c) {
        return tx.run(() -> {
            Partner partner = Partner.register(c.name().trim(), PartnerMapper.toDomain(c.type()), c.contactPerson(),
                    c.phone(), c.email(), c.address(), c.taxId());
            PartnerDTO saved = PartnerMapper.toDto(partners.save(partner));
            events.publish(new PartnerCreated(saved));
            return saved;
        });
    }
}
