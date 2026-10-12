package org.example.erp.modules.accounting.domain;

import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record JournalLine(String accountCode, BigDecimal debit, BigDecimal credit, Long partnerId, String memo) {

    public JournalLine {
        debit = scaled(debit);
        credit = scaled(credit);
        if (debit.signum() < 0 || credit.signum() < 0) throw new BusinessRuleException("Amounts cannot be negative");
        if ((debit.signum() > 0) == (credit.signum() > 0))
            throw new BusinessRuleException("Each line needs either a debit or a credit amount");
    }

    private static BigDecimal scaled(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public JournalLine swapped(String memo) {
        return new JournalLine(accountCode, credit, debit, partnerId, memo);
    }
}
