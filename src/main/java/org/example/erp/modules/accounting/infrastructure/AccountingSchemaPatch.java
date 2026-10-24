package org.example.erp.modules.accounting.infrastructure;

import jakarta.persistence.EntityManagerFactory;
import org.example.erp.platform.LegacyEnumColumns;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Lets databases created before the SALES_RETURN, PURCHASE, SUPPLIER_PAYMENT, EXPENSE and OTHER_INCOME entry origins
 * existed store them.
 * Depends on the EntityManagerFactory so it runs after Hibernate has updated the schema.
 */
@Component
class AccountingSchemaPatch {
    AccountingSchemaPatch(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
        LegacyEnumColumns.relax(dataSource, "accounting_journal_entries", "origin");
    }
}
