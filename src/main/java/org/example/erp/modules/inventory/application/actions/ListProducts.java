package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.domain.ProductRepository;

import java.util.List;

public final class ListProducts {
    private final ProductRepository products;

    public ListProducts(ProductRepository products) {
        this.products = products;
    }

    public List<ProductDTO> handle() {
        return products.findAllByName().stream().map(InventoryMapper::toDto).toList();
    }
}
