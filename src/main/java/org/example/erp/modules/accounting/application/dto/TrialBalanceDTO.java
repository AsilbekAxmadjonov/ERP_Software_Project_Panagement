package org.example.erp.modules.accounting.application.dto;

import org.example.erp.modules.accounting.application.enums.AccountKind;

import java.math.BigDecimal;
import java.util.List;

public record TrialBalanceDTO(List<Row> rows, BigDecimal totalDebit, BigDecimal totalCredit) {
    public record Row(String code, String name, AccountKind type, BigDecimal debit, BigDecimal credit) {}
}
