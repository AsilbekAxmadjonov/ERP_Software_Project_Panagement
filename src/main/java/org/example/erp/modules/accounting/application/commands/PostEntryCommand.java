package org.example.erp.modules.accounting.application.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PostEntryCommand(LocalDate date, String reference, String description, List<Line> lines) {
    public record Line(String accountCode, BigDecimal debit, BigDecimal credit, Long partnerId, String memo) {
        public static Line debit(String accountCode, BigDecimal amount, Long partnerId, String memo) {
            return new Line(accountCode, amount, BigDecimal.ZERO, partnerId, memo);
        }

        public static Line credit(String accountCode, BigDecimal amount, Long partnerId, String memo) {
            return new Line(accountCode, BigDecimal.ZERO, amount, partnerId, memo);
        }
    }
}
