package org.example.erp.composition;

import org.example.erp.modules.inventory.application.actions.ReceiveStock;
import org.example.erp.modules.inventory.application.commands.ReceiveStockCommand;
import org.example.erp.modules.inventory.application.enums.StockReceipt;
import org.example.erp.modules.sales.application.contracts.StockReturner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
class StockReturnerAdapter implements StockReturner {
    private final ReceiveStock receiveStock;

    StockReturnerAdapter(ReceiveStock receiveStock) {
        this.receiveStock = receiveStock;
    }

    @Override
    public void returnToStock(long productId, BigDecimal quantity, BigDecimal unitCost, String reference, LocalDate date) {
        receiveStock.handle(new ReceiveStockCommand(productId, quantity, unitCost, StockReceipt.SALES_RETURN, reference, date));
    }
}
