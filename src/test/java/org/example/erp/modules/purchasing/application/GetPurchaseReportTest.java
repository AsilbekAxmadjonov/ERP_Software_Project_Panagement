package org.example.erp.modules.purchasing.application;

import org.example.erp.modules.purchasing.application.actions.GetPurchaseReport;
import org.example.erp.modules.purchasing.application.dto.PurchaseReportRowDTO;
import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.PurchaseOrderLine;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.modules.purchasing.domain.PurchaseOrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetPurchaseReportTest {
    private static final long GRAIN_CO = 7;
    private static final long OIL_CO = 8;
    private static final long FLOUR = 1;
    private static final long OIL = 2;

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private static PurchaseOrder order(long id, long supplierId, String supplier, PurchaseOrderStatus status, LocalDate received,
                                       PurchaseOrderLine... lines) {
        return PurchaseOrder.restore(id, "PO-%05d".formatted(id), supplierId, supplier, LocalDate.of(2026, 9, 1), List.of(lines),
                status, received, BigDecimal.ZERO, "manager");
    }

    private static final PurchaseOrderLine FLOUR_LINE = new PurchaseOrderLine(FLOUR, "Flour", "KG", n("100"), n("2000"));
    private static final PurchaseOrderLine OIL_LINE = new PurchaseOrderLine(OIL, "Oil", "PCS", n("10"), n("12000"));

    private final GetPurchaseReport report = new GetPurchaseReport(new InMemoryOrders(List.of(
            order(4, GRAIN_CO, "Grain Co", PurchaseOrderStatus.RECEIVED, LocalDate.of(2026, 10, 15), FLOUR_LINE, OIL_LINE),
            order(3, OIL_CO, "Oil Co", PurchaseOrderStatus.RECEIVED, LocalDate.of(2026, 9, 20), OIL_LINE),
            order(2, GRAIN_CO, "Grain Co", PurchaseOrderStatus.CONFIRMED, null, FLOUR_LINE),
            order(1, GRAIN_CO, "Grain Co", PurchaseOrderStatus.CANCELLED, null, FLOUR_LINE))));

    @Test
    void listsOnlyItemsOfReceivedOrdersDatedByReceipt() {
        List<PurchaseReportRowDTO> rows = report.handle(null, null, null, null);

        assertEquals(List.of("PO-00004", "PO-00004", "PO-00003"), rows.stream().map(PurchaseReportRowDTO::order).toList());
        PurchaseReportRowDTO flour = rows.getFirst();
        assertEquals(LocalDate.of(2026, 10, 15), flour.date());
        assertEquals(0, n("200000").compareTo(flour.total()));
        assertEquals(0, n("440000").compareTo(rows.stream().map(PurchaseReportRowDTO::total).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @Test
    void filtersByReceiptDateRangeInclusively() {
        assertEquals(List.of("PO-00003"), orders(report.handle(LocalDate.of(2026, 9, 20), LocalDate.of(2026, 10, 14), null, null)));
        assertEquals(List.of("PO-00004", "PO-00004"), orders(report.handle(LocalDate.of(2026, 10, 1), null, null, null)));
        assertEquals(List.of(), orders(report.handle(null, LocalDate.of(2026, 9, 19), null, null)));
    }

    @Test
    void filtersBySupplierAndProduct() {
        assertEquals(List.of("PO-00003"), orders(report.handle(null, null, OIL_CO, null)));
        assertEquals(List.of("Oil", "Oil"), report.handle(null, null, null, OIL).stream().map(PurchaseReportRowDTO::product).toList());
        List<PurchaseReportRowDTO> grainFlour = report.handle(null, null, GRAIN_CO, FLOUR);
        assertEquals(1, grainFlour.size());
        assertEquals("Grain Co", grainFlour.getFirst().supplier());
    }

    private static List<String> orders(List<PurchaseReportRowDTO> rows) {
        return rows.stream().map(PurchaseReportRowDTO::order).toList();
    }

    private record InMemoryOrders(List<PurchaseOrder> all) implements PurchaseOrderRepository {
        @Override
        public Optional<PurchaseOrder> findById(long id) {
            return all.stream().filter(o -> o.getId() == id).findFirst();
        }

        @Override
        public List<PurchaseOrder> findAllNewestFirst() {
            return all;
        }

        @Override
        public PurchaseOrder save(PurchaseOrder order) {
            throw new UnsupportedOperationException();
        }
    }
}
