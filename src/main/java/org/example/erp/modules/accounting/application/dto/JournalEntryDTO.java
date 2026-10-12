package org.example.erp.modules.accounting.application.dto;

import org.example.erp.modules.accounting.application.enums.EntryKind;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JournalEntryDTO(long id, LocalDate entryDate, String reference, String description, EntryKind origin,
                              boolean reversed, Long reversalOf, String createdBy, BigDecimal total, List<JournalLineDTO> lines) {}
