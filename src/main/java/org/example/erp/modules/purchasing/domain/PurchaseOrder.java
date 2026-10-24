package org.example.erp.modules.purchasing.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DRAFT -> CONFIRMED -> RECEIVED, or CANCELLED from DRAFT or CONFIRMED. Receiving takes in the full ordered quantity
 * and makes the order total payable to the supplier; only then can payments be applied to it.
 */
@Getter
public final class PurchaseOrder {
    private final Long id;
    private final String number;
    private final long supplierId;
    private final String supplierName;
    private final LocalDate orderDate;
    private final List<PurchaseOrderLine> lines;
    private final BigDecimal total;
    private PurchaseOrderStatus status;
    private LocalDate receivedDate;
    private BigDecimal paid;
    private final String createdBy;

    private PurchaseOrder(Long id, String number, long supplierId, String supplierName, LocalDate orderDate,
                          List<PurchaseOrderLine> lines, PurchaseOrderStatus status, LocalDate receivedDate,
                          BigDecimal paid, String createdBy) {
        this.id = id;
        this.number = number;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.orderDate = orderDate;
        this.lines = List.copyOf(lines);
        this.total = lines.stream().map(PurchaseOrderLine::lineTotal).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        this.status = status;
        this.receivedDate = receivedDate;
        this.paid = paid;
        this.createdBy = createdBy;
    }

    public static PurchaseOrder create(String number, long supplierId, String supplierName, LocalDate orderDate,
                                       List<PurchaseOrderLine> lines, String createdBy) {
        if (lines.isEmpty()) throw new BusinessRuleException("Add at least one item");
        PurchaseOrder order = new PurchaseOrder(null, number, supplierId, supplierName, orderDate, lines,
                PurchaseOrderStatus.DRAFT, null, BigDecimal.ZERO.setScale(2), createdBy);
        if (order.total.signum() <= 0) throw new BusinessRuleException("Order total must be greater than zero");
        return order;
    }

    public static PurchaseOrder restore(Long id, String number, long supplierId, String supplierName, LocalDate orderDate,
                                        List<PurchaseOrderLine> lines, PurchaseOrderStatus status, LocalDate receivedDate,
                                        BigDecimal paid, String createdBy) {
        return new PurchaseOrder(id, number, supplierId, supplierName, orderDate, lines, status, receivedDate, paid, createdBy);
    }

    public void confirm() {
        if (status != PurchaseOrderStatus.DRAFT) throw new BusinessRuleException("Only a draft order can be confirmed; " + number + " is " + status);
        this.status = PurchaseOrderStatus.CONFIRMED;
    }

    public void receive(LocalDate date) {
        if (status != PurchaseOrderStatus.CONFIRMED)
            throw new BusinessRuleException("Only a confirmed order can be received; " + number + " is " + status);
        if (date.isBefore(orderDate)) throw new BusinessRuleException("Goods cannot be received before the order date");
        this.status = PurchaseOrderStatus.RECEIVED;
        this.receivedDate = date;
    }

    public void cancel() {
        if (status != PurchaseOrderStatus.DRAFT && status != PurchaseOrderStatus.CONFIRMED)
            throw new BusinessRuleException("Only a draft or confirmed order can be cancelled; " + number + " is " + status);
        this.status = PurchaseOrderStatus.CANCELLED;
    }

    /** What the supplier has billed us: the order total once the goods are received. */
    public BigDecimal billed() {
        return status == PurchaseOrderStatus.RECEIVED ? total : BigDecimal.ZERO.setScale(2);
    }

    public BigDecimal outstanding() {
        return billed().subtract(paid);
    }

    public void applyPayment(BigDecimal amount) {
        if (status != PurchaseOrderStatus.RECEIVED)
            throw new BusinessRuleException("Only received orders can be paid; record an advance without an order instead");
        if (amount.compareTo(outstanding()) > 0)
            throw new BusinessRuleException("Payment exceeds the outstanding amount of " + outstanding());
        this.paid = paid.add(amount);
    }
}
