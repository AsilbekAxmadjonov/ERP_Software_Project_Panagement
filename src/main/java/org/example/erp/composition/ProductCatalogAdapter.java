package org.example.erp.composition;

import org.example.erp.modules.inventory.application.actions.FindProduct;
import org.example.erp.modules.sales.application.contracts.ProductCatalog;
import org.example.erp.platform.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class ProductCatalogAdapter implements ProductCatalog {
    private final FindProduct findProduct;

    ProductCatalogAdapter(FindProduct findProduct) {
        this.findProduct = findProduct;
    }

    @Override
    public Optional<ProductInfo> find(long productId) {
        try {
            var product = findProduct.handle(productId);
            return Optional.of(new ProductInfo(product.id(), product.name(), product.unit().name(), product.salePrice(), product.active()));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
