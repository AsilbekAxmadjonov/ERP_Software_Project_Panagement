package org.example.erp.modules.inventory.application.events;

import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

/** Carries the product's master data after the edit. */
public final class ProductUpdated extends BaseIntegrationEvent {
    private final ProductDTO product;

    public ProductUpdated(ProductDTO product) {
        super(null, null);
        this.product = product;
    }

    public ProductDTO product() {
        return product;
    }

    @Override
    public String eventName() {
        return "inventory.product-updated";
    }

    @Override
    public Map<String, Object> payload() {
        return ProductPayload.of(product);
    }
}
