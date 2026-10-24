package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

public final class UpdateProduct {
    private final ProductRepository products;
    private final TransactionRunner tx;

    public UpdateProduct(ProductRepository products, TransactionRunner tx) {
        this.products = products;
        this.tx = tx;
    }

    public ProductDTO handle(long id, SaveProductCommand c) {
        return tx.run(() -> {
            Product product = products.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
            product.update(c.name().trim(), InventoryMapper.toDomain(c.unit()), c.salePrice(), c.active(),
                    InventoryMapper.quantity(c.reorderLevel()));
            return InventoryMapper.toDto(products.save(product));
        });
    }
}
