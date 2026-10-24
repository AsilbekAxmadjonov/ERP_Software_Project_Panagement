package org.example.erp.modules.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Income statement for entries dated within [from, to]: netProfit = totalRevenue - totalExpenses. */
public record ProfitAndLossDTO(LocalDate from, LocalDate to, List<StatementLineDTO> revenue, BigDecimal totalRevenue,
                               List<StatementLineDTO> expenses, BigDecimal totalExpenses, BigDecimal netProfit) {}
