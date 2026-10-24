package org.example.erp.modules.inventory.application.events;

import org.example.erp.modules.inventory.application.dto.ProductDTO;

import java.util.LinkedHashMap;
import java.util.Map;

final class ProductPayload {
    private ProductPayload() {}

    /** Master data only; stock quantity and value change through movements, which have their own events. */
    static Map<String, Object> of(ProductDTO p) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("productId", p.id());
        payload.put("code", p.code());
        payload.put("productName", p.name());
        payload.put("unit", p.unit().name());
        payload.put("salePrice", p.salePrice().toPlainString());
        payload.put("reorderLevel", p.reorderLevel() == null ? null : p.reorderLevel().toPlainString());
        payload.put("active", p.active());
        return payload;
    }
}
