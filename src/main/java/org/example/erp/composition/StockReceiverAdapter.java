package org.example.erp.composition;

import org.example.erp.modules.inventory.application.actions.ReceiveStock;
import org.example.erp.modules.inventory.application.commands.ReceiveStockCommand;
import org.example.erp.modules.inventory.application.enums.StockReceipt;
import org.example.erp.modules.purchasing.application.contracts.StockReceiver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
class StockReceiverAdapter implements StockReceiver {
    private final ReceiveStock receiveStock;

    StockReceiverAdapter(ReceiveStock receiveStock) {
        this.receiveStock = receiveStock;
    }

    @Override
    public BigDecimal receive(long productId, BigDecimal quantity, BigDecimal unitCost, String reference, LocalDate date) {
        return receiveStock.handle(new ReceiveStockCommand(productId, quantity, unitCost, StockReceipt.PURCHASE, reference, date));
    }
}
