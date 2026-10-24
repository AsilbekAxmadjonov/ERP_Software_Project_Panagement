package org.example.erp.modules.partners.infrastructure;

import org.example.erp.modules.partners.application.actions.*;
import org.example.erp.modules.partners.domain.PartnerRepository;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PartnersModuleConfig {

    @Bean
    ListPartners listPartners(PartnerRepository partners) {
        return new ListPartners(partners);
    }

    @Bean
    public FindPartner findPartner(PartnerRepository partners) {
        return new FindPartner(partners);
    }

    @Bean
    CreatePartner createPartner(PartnerRepository partners, IntegrationEventPublisher events, TransactionRunner tx) {
        return new CreatePartner(partners, events, tx);
    }

    @Bean
    UpdatePartner updatePartner(PartnerRepository partners, IntegrationEventPublisher events, TransactionRunner tx) {
        return new UpdatePartner(partners, events, tx);
    }
}
