package org.example.erp.modules.inventory.infrastructure;

import jakarta.persistence.EntityManagerFactory;
import org.example.erp.platform.LegacyEnumColumns;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Lets databases created before PURCHASE and SALES_RETURN movements existed store them. Depends on the
 * EntityManagerFactory so it runs after Hibernate has updated the schema.
 */
@Component
class InventorySchemaPatch {
    InventorySchemaPatch(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
        LegacyEnumColumns.relax(dataSource, "inventory_stock_movements", "type");
    }
}
