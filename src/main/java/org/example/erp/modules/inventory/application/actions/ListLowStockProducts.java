package org.example.erp.modules.inventory.application.actions;

import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.ProductRepository;

import java.util.List;

/** Active products whose quantity is at or below their reorder level. */
public final class ListLowStockProducts {
    private final ProductRepository products;

    public ListLowStockProducts(ProductRepository products) {
        this.products = products;
    }

    public List<ProductDTO> handle() {
        return products.findAllByName().stream().filter(Product::isActive).filter(Product::isLowStock)
                .map(InventoryMapper::toDto).toList();
    }
}
