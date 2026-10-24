package org.example.erp.platform;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Hibernate maps {@code @Enumerated(STRING)} columns to a native ENUM (H2) or a CHECK constraint (PostgreSQL) listing
 * the values known when the table was first created. {@code ddl-auto: update} never widens those, so adding an enum
 * value would break databases created by an earlier version. This turns such a column into a plain VARCHAR; the
 * value set is still enforced by the Java enum. Safe to run on every start: it only changes the column type.
 */
public final class LegacyEnumColumns {
    private LegacyEnumColumns() {}

    public static void relax(DataSource dataSource, String table, String column) {
        try (Connection c = dataSource.getConnection()) {
            String product = c.getMetaData().getDatabaseProductName().toLowerCase();
            if (product.contains("h2")) relaxH2(c, table, column);
            else if (product.contains("postgres")) relaxPostgres(c, table, column);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not relax enum column " + table + "." + column, e);
        }
    }

    private static void relaxH2(Connection c, String table, String column) throws SQLException {
        try (PreparedStatement q = c.prepareStatement("select data_type from information_schema.columns "
                + "where upper(table_name) = ? and upper(column_name) = ?")) {
            q.setString(1, table.toUpperCase());
            q.setString(2, column.toUpperCase());
            try (ResultSet r = q.executeQuery()) {
                if (!r.next() || !"ENUM".equalsIgnoreCase(r.getString(1))) return;
            }
        }
        try (Statement s = c.createStatement()) {
            s.execute("alter table " + table + " alter column " + column + " set data type varchar(64)");
        }
    }

    private static void relaxPostgres(Connection c, String table, String column) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("alter table " + table + " drop constraint if exists " + table + "_" + column + "_check");
        }
    }
}
