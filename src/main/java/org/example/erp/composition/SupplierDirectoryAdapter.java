package org.example.erp.composition;

import org.example.erp.modules.partners.application.actions.FindPartner;
import org.example.erp.modules.purchasing.application.contracts.SupplierDirectory;
import org.example.erp.platform.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SupplierDirectoryAdapter implements SupplierDirectory {
    private final FindPartner findPartner;

    SupplierDirectoryAdapter(FindPartner findPartner) {
        this.findPartner = findPartner;
    }

    @Override
    public Optional<SupplierInfo> find(long supplierId) {
        try {
            var partner = findPartner.handle(supplierId);
            return Optional.of(new SupplierInfo(partner.id(), partner.name(), partner.supplier(), partner.active()));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
