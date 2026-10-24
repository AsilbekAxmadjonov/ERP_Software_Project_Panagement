package org.example.erp.modules.accounting.application.dto;

import org.example.erp.modules.accounting.application.enums.EntryKind;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cash (Kassa) and bank movements within [from, to]. Totals add up per account: opening + inflow - outflow = closing.
 * A transfer between cash and bank counts as an outflow of one account and an inflow of the other.
 */
public record CashFlowDTO(LocalDate from, LocalDate to, List<AccountFlow> accounts,
                          BigDecimal opening, BigDecimal inflow, BigDecimal outflow, BigDecimal closing,
                          List<OriginTotal> inflowsByOrigin, List<OriginTotal> outflowsByOrigin,
                          List<Day> days, List<Movement> movements) {

    public record AccountFlow(String code, String name, BigDecimal opening, BigDecimal inflow, BigDecimal outflow, BigDecimal closing) {}

    /** entries counts the distinct journal entries behind the amount. */
    public record OriginTotal(EntryKind origin, BigDecimal amount, int entries) {}

    public record Day(LocalDate date, BigDecimal inflow, BigDecimal outflow, BigDecimal closing) {}

    /** balance is the running total of all cash accounts after this movement. */
    public record Movement(long entryId, LocalDate date, String reference, String description, EntryKind origin,
                           String accountCode, String accountName, BigDecimal inflow, BigDecimal outflow, BigDecimal balance) {}
}
