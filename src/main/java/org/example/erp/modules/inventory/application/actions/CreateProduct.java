package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.application.events.ProductCreated;
import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;

public final class CreateProduct {
    private final ProductRepository products;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;

    public CreateProduct(ProductRepository products, IntegrationEventPublisher events, TransactionRunner tx) {
        this.products = products;
        this.events = events;
        this.tx = tx;
    }

    public ProductDTO handle(SaveProductCommand c) {
        String code = c.code().trim();
        return tx.run(() -> {
            if (products.existsByCode(code)) throw new BusinessRuleException("Product code already exists");
            Product product = Product.create(code, c.name().trim(), InventoryMapper.toDomain(c.unit()), c.salePrice(),
                    InventoryMapper.quantity(c.reorderLevel()));
            ProductDTO saved = InventoryMapper.toDto(products.save(product));
            events.publish(new ProductCreated(saved));
            return saved;
        });
    }
}
