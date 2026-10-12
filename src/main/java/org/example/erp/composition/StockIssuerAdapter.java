package org.example.erp.composition;

import org.example.erp.modules.inventory.application.actions.IssueStock;
import org.example.erp.modules.inventory.application.commands.IssueStockCommand;
import org.example.erp.modules.sales.application.contracts.StockIssuer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
class StockIssuerAdapter implements StockIssuer {
    private final IssueStock issueStock;

    StockIssuerAdapter(IssueStock issueStock) {
        this.issueStock = issueStock;
    }

    @Override
    public BigDecimal issue(long productId, BigDecimal quantity, String reference, LocalDate date) {
        return issueStock.handle(new IssueStockCommand(productId, quantity, reference, date));
    }
}
