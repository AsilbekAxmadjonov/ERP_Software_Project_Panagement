package org.example.erp.modules.sales.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * credited is the value of goods returned on credit notes. It settles the invoice like a payment; whatever
 * paid plus credited exceeds the total is credit the customer holds with us.
 */
@Getter
public final class Invoice {
    private final Long id;
    private final String number;
    private final long customerId;
    private final String customerName;
    private final LocalDate invoiceDate;
    private List<InvoiceLine> lines;
    private final BigDecimal total;
    private final BigDecimal cost;
    private BigDecimal paid;
    private BigDecimal credited;
    private final String createdBy;

    private Invoice(Long id, String number, long customerId, String customerName, LocalDate invoiceDate,
                    List<InvoiceLine> lines, BigDecimal paid, BigDecimal credited, String createdBy) {
        this.id = id;
        this.number = number;
        this.customerId = customerId;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.lines = List.copyOf(lines);
        this.total = lines.stream().map(InvoiceLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.cost = lines.stream().map(InvoiceLine::lineCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.paid = paid;
        this.credited = credited;
        this.createdBy = createdBy;
    }

    public static Invoice issue(String number, long customerId, String customerName, LocalDate date,
                                List<InvoiceLine> lines, String createdBy) {
        if (lines.isEmpty()) throw new BusinessRuleException("Add at least one item");
        Invoice invoice = new Invoice(null, number, customerId, customerName, date, lines, BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2), createdBy);
        if (invoice.total.signum() <= 0) throw new BusinessRuleException("Invoice total must be greater than zero");
        return invoice;
    }

    public static Invoice restore(Long id, String number, long customerId, String customerName, LocalDate date,
                                  List<InvoiceLine> lines, BigDecimal paid, BigDecimal credited, String createdBy) {
        return new Invoice(id, number, customerId, customerName, date, lines, paid, credited, createdBy);
    }

    public BigDecimal outstanding() {
        return total.subtract(paid).subtract(credited).max(BigDecimal.ZERO.setScale(2));
    }

    public boolean isFullyReturned() {
        return lines.stream().allMatch(l -> l.returnable().signum() == 0);
    }

    public void applyPayment(BigDecimal amount) {
        if (amount.compareTo(outstanding()) > 0)
            throw new BusinessRuleException("Payment exceeds the outstanding amount of " + outstanding());
        this.paid = paid.add(amount);
    }

    /**
     * Takes goods back at the price and cost they were sold at. A product sold on several lines is returned from the
     * first line that still has quantity left. Each line's amount is the difference of rounded running totals, so
     * returning everything in several steps credits exactly the line total.
     */
    public CreditNote creditReturn(String creditNoteNumber, LocalDate date, String reason, List<ReturnItem> items, String createdBy) {
        if (items.isEmpty()) throw new BusinessRuleException("Add at least one item to return");
        if (date.isBefore(invoiceDate)) throw new BusinessRuleException("A credit note cannot be dated before its invoice");
        List<InvoiceLine> updated = new ArrayList<>(lines);
        List<CreditNoteLine> credit = new ArrayList<>();
        for (ReturnItem item : items) {
            if (item.quantity().signum() <= 0) throw new BusinessRuleException("Return quantity must be greater than zero");
            List<Integer> matching = new ArrayList<>();
            for (int i = 0; i < updated.size(); i++) if (updated.get(i).productId() == item.productId()) matching.add(i);
            if (matching.isEmpty()) throw new BusinessRuleException("Product " + item.productId() + " is not on invoice " + number);
            BigDecimal available = matching.stream().map(i -> updated.get(i).returnable()).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (item.quantity().compareTo(available) > 0)
                throw new BusinessRuleException("Cannot return " + item.quantity() + " of " + updated.get(matching.getFirst()).productName()
                        + ": only " + available + " left to return on " + number);

            BigDecimal left = item.quantity();
            for (int i : matching) {
                if (left.signum() == 0) break;
                InvoiceLine line = updated.get(i);
                BigDecimal take = left.min(line.returnable());
                if (take.signum() == 0) continue;
                BigDecimal before = line.returnedQuantity();
                BigDecimal after = before.add(take);
                credit.add(new CreditNoteLine(line.productId(), line.productName(), line.unit(), take, line.unitPrice(),
                        line.unitCost(), line.amountOf(after).subtract(line.amountOf(before)),
                        line.costOf(after).subtract(line.costOf(before))));
                updated.set(i, line.withReturned(after));
                left = left.subtract(take);
            }
        }
        CreditNote note = CreditNote.issue(creditNoteNumber, this, date, reason, credit, createdBy);
        this.lines = List.copyOf(updated);
        this.credited = credited.add(note.getTotal());
        return note;
    }
}
