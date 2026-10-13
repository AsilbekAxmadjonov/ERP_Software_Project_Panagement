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

/** Customers with what they owe: partner data from one module, the balance from the ledger of another. */
@RestController
@RequestMapping("/api/customer-balances")
class CustomerBalancesController {
    private final ListPartners listPartners;
    private final GetPartnerBalance partnerBalance;

    CustomerBalancesController(ListPartners listPartners, GetPartnerBalance partnerBalance) {
        this.listPartners = listPartners;
        this.partnerBalance = partnerBalance;
    }

    record CustomerBalance(long partnerId, String name, String phone, BigDecimal balance) {}

    @GetMapping
    List<CustomerBalance> list() {
        return listPartners.handle().stream()
                .filter(PartnerDTO::customer)
                .map(p -> new CustomerBalance(p.id(), p.name(), p.phone(), partnerBalance.handle(p.id(), LedgerRole.RECEIVABLE)))
                .toList();
    }
}
