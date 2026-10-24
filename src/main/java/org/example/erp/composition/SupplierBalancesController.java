package org.example.erp.composition;

import org.example.erp.modules.accounting.application.actions.GetPartnerBalance;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.partners.application.actions.ListPartners;
import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Suppliers with what we owe them: positive balance is owed to the supplier, negative is an advance we paid. */
@RestController
@RequestMapping("/api/supplier-balances")
class SupplierBalancesController {
    private final ListPartners listPartners;
    private final GetPartnerBalance partnerBalance;

    SupplierBalancesController(ListPartners listPartners, GetPartnerBalance partnerBalance) {
        this.listPartners = listPartners;
        this.partnerBalance = partnerBalance;
    }

    record SupplierBalance(long partnerId, String name, String phone, BigDecimal balance) {}

    @GetMapping
    List<SupplierBalance> list() {
        return listPartners.handle().stream()
                .filter(PartnerDTO::supplier)
                .map(p -> new SupplierBalance(p.id(), p.name(), p.phone(),
                        partnerBalance.handle(p.id(), LedgerRole.PAYABLE).negate()))
                .toList();
    }
}
