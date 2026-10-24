package org.example.erp.composition;

import org.example.erp.modules.inventory.application.actions.FindProduct;
import org.example.erp.modules.purchasing.application.contracts.PurchasableProducts;
import org.example.erp.platform.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class PurchasableProductsAdapter implements PurchasableProducts {
    private final FindProduct findProduct;

    PurchasableProductsAdapter(FindProduct findProduct) {
        this.findProduct = findProduct;
    }

    @Override
    public Optional<ProductInfo> find(long productId) {
        try {
            var product = findProduct.handle(productId);
            return Optional.of(new ProductInfo(product.id(), product.name(), product.unit().name(), product.active()));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
