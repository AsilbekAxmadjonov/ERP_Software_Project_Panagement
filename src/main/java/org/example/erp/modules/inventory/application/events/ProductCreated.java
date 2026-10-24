package org.example.erp.modules.inventory.application.events;

import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

public final class ProductCreated extends BaseIntegrationEvent {
    private final ProductDTO product;

    public ProductCreated(ProductDTO product) {
        super(null, null);
        this.product = product;
    }

    public ProductDTO product() {
        return product;
    }

    @Override
    public String eventName() {
        return "inventory.product-created";
    }

    @Override
    public Map<String, Object> payload() {
        return ProductPayload.of(product);
    }
}
