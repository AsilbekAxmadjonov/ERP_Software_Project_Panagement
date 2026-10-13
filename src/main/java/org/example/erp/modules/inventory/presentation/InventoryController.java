package org.example.erp.modules.inventory.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.erp.modules.inventory.application.actions.*;
import org.example.erp.modules.inventory.application.commands.AdjustStockCommand;
import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.application.dto.StockMovementDTO;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
class InventoryController {
    private final ListProducts listProducts;
    private final CreateProduct createProduct;
    private final UpdateProduct updateProduct;
    private final AdjustStock adjustStock;
    private final ListStockMovements listMovements;

    InventoryController(ListProducts listProducts, CreateProduct createProduct, UpdateProduct updateProduct,
                        AdjustStock adjustStock, ListStockMovements listMovements) {
        this.listProducts = listProducts;
        this.createProduct = createProduct;
        this.updateProduct = updateProduct;
        this.adjustStock = adjustStock;
        this.listMovements = listMovements;
    }

    record ProductRequest(@NotBlank String code, @NotBlank String name, @NotNull UnitOfMeasure unit,
                          @NotNull BigDecimal salePrice, Boolean active) {
        SaveProductCommand toCommand() {
            return new SaveProductCommand(code, name, unit, salePrice, active == null || active);
        }
    }

    record AdjustmentRequest(@NotNull Long productId, @NotNull BigDecimal quantity, BigDecimal unitCost, @NotBlank String reason) {}

    @GetMapping("/products")
    List<ProductDTO> products() {
        return listProducts.handle();
    }

    @PostMapping("/products")
    ProductDTO create(@RequestBody @Valid ProductRequest r) {
        return createProduct.handle(r.toCommand());
    }

    @PutMapping("/products/{id}")
    ProductDTO update(@PathVariable long id, @RequestBody @Valid ProductRequest r) {
        return updateProduct.handle(id, r.toCommand());
    }

    @GetMapping("/movements")
    List<StockMovementDTO> movements(@RequestParam(required = false) Long productId) {
        return listMovements.handle(productId);
    }

    @PostMapping("/adjustments")
    ProductDTO adjust(@RequestBody @Valid AdjustmentRequest r) {
        return adjustStock.handle(new AdjustStockCommand(r.productId(), r.quantity(), r.unitCost(), r.reason()));
    }
}
