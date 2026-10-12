package org.example.erp.modules.accounting.application.dto;

import java.math.BigDecimal;

public record JournalLineDTO(String accountCode, String accountName, BigDecimal debit, BigDecimal credit, Long partnerId, String memo) {}
