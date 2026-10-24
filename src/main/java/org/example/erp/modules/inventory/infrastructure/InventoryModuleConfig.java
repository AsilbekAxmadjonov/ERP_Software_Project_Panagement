package org.example.erp.modules.inventory.infrastructure;

import org.example.erp.modules.inventory.application.actions.*;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.example.erp.modules.inventory.domain.StockMovementRepository;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class InventoryModuleConfig {

    @Bean
    ListProducts listProducts(ProductRepository products) {
        return new ListProducts(products);
    }

    @Bean
    public FindProduct findProduct(ProductRepository products) {
        return new FindProduct(products);
    }

    @Bean
    CreateProduct createProduct(ProductRepository products, TransactionRunner tx) {
        return new CreateProduct(products, tx);
    }

    @Bean
    UpdateProduct updateProduct(ProductRepository products, TransactionRunner tx) {
        return new UpdateProduct(products, tx);
    }

    @Bean
    AdjustStock adjustStock(ProductRepository products, StockMovementRepository movements, IntegrationEventPublisher events,
                            TransactionRunner tx, Clock clock) {
        return new AdjustStock(products, movements, events, tx, clock);
    }

    @Bean
    public IssueStock issueStock(ProductRepository products, StockMovementRepository movements, TransactionRunner tx) {
        return new IssueStock(products, movements, tx);
    }

    @Bean
    public ReceiveStock receiveStock(ProductRepository products, StockMovementRepository movements, TransactionRunner tx) {
        return new ReceiveStock(products, movements, tx);
    }

    @Bean
    ListLowStockProducts listLowStockProducts(ProductRepository products) {
        return new ListLowStockProducts(products);
    }

    @Bean
    ListStockMovements listStockMovements(StockMovementRepository movements) {
        return new ListStockMovements(movements);
    }
}
