package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.AccountDTO;
import org.example.erp.modules.accounting.application.dto.JournalEntryDTO;
import org.example.erp.modules.accounting.application.dto.JournalLineDTO;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.JournalEntry;

import java.util.Map;

final class AccountingMapper {
    private AccountingMapper() {}

    static AccountDTO toDto(Account a) {
        return new AccountDTO(a.getId(), a.getCode(), a.getName(), AccountKind.valueOf(a.getType().name()), a.isActive());
    }

    static JournalEntryDTO toDto(JournalEntry e, Map<String, String> accountNames) {
        return new JournalEntryDTO(e.getId(), e.getEntryDate(), e.getReference(), e.getDescription(),
                EntryKind.valueOf(e.getOrigin().name()), e.isReversed(), e.getReversalOf(), e.getCreatedBy(), e.total(),
                e.getLines().stream()
                        .map(l -> new JournalLineDTO(l.accountCode(), accountNames.get(l.accountCode()), l.debit(), l.credit(), l.partnerId(), l.memo()))
                        .toList());
    }
}
