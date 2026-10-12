package org.example.erp.modules.inventory.application.dto;

import org.example.erp.modules.inventory.application.enums.MovementKind;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockMovementDTO(long id, long productId, String productName, LocalDate date, MovementKind type,
                               BigDecimal quantity, BigDecimal unitCost, String reference, String reason) {}
