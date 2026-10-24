package org.example.erp.modules.accounting.application.dto;

import org.example.erp.modules.accounting.application.enums.AccountKind;

import java.math.BigDecimal;

/** One account on a financial statement; amount is signed on the side where the account kind grows. */
public record StatementLineDTO(String code, String name, AccountKind type, BigDecimal amount) {}
