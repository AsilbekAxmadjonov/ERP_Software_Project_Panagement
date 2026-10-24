package org.example.erp.modules.sales.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreditNoteDTO(long id, String number, long invoiceId, String invoiceNumber, long customerId,
                            String customerName, LocalDate date, String reason, BigDecimal total, BigDecimal cost,
                            String createdBy, List<CreditNoteLineDTO> lines) {}
