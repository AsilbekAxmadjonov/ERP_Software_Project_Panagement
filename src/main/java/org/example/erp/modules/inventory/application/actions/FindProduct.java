package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.example.erp.platform.NotFoundException;

public final class FindProduct {
    private final ProductRepository products;

    public FindProduct(ProductRepository products) {
        this.products = products;
    }

    public ProductDTO handle(long id) {
        return products.findById(id).map(InventoryMapper::toDto).orElseThrow(() -> new NotFoundException("Product not found"));
    }
}
