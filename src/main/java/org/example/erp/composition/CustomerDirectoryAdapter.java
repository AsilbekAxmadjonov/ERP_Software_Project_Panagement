package org.example.erp.composition;

import org.example.erp.modules.partners.application.actions.FindPartner;
import org.example.erp.modules.sales.application.contracts.CustomerDirectory;
import org.example.erp.platform.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class CustomerDirectoryAdapter implements CustomerDirectory {
    private final FindPartner findPartner;

    CustomerDirectoryAdapter(FindPartner findPartner) {
        this.findPartner = findPartner;
    }

    @Override
    public Optional<CustomerInfo> find(long customerId) {
        try {
            var partner = findPartner.handle(customerId);
            return Optional.of(new CustomerInfo(partner.id(), partner.name(), partner.customer(), partner.active()));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
