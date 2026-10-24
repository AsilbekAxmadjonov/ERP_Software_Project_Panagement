package org.example.erp.modules.expenses.application.actions;

import org.example.erp.modules.expenses.application.contracts.LedgerAccounts;
import org.example.erp.modules.expenses.application.dto.CashEntryDTO;
import org.example.erp.modules.expenses.application.dto.CategoryDTO;
import org.example.erp.modules.expenses.application.enums.EntryState;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;
import org.example.erp.modules.expenses.domain.CashEntry;
import org.example.erp.modules.expenses.domain.Category;
import org.example.erp.modules.expenses.domain.CategoryKind;
import org.example.erp.platform.BusinessRuleException;

final class ExpensesMapper {
    private ExpensesMapper() {}

    static CategoryKind toKind(EntryType type) {
        return switch (type) {
            case EXPENSE -> CategoryKind.EXPENSE;
            case INCOME -> CategoryKind.INCOME;
        };
    }

    static EntryType toType(CategoryKind kind) {
        return switch (kind) {
            case EXPENSE -> EntryType.EXPENSE;
            case INCOME -> EntryType.INCOME;
        };
    }

    static CategoryDTO toDto(Category c) {
        return new CategoryDTO(c.getId(), c.getName(), toType(c.getKind()), c.getAccountCode(), c.isActive());
    }

    static CashEntryDTO toDto(CashEntry e) {
        return new CashEntryDTO(e.getId(), e.reference(), toType(e.getKind()), e.getCategoryId(), e.getCategoryName(),
                e.getAccountCode(), e.getDate(), e.getAmount(), PaymentChannel.valueOf(e.getMethod().name()), e.getDescription(),
                EntryState.valueOf(e.getStatus().name()), e.getCancelledOn(), e.getCreatedBy());
    }

    /** Categories may only post to an active account of the matching nature (expense or revenue). */
    static void ensurePostable(LedgerAccounts ledger, EntryType type, String accountCode) {
        String code = accountCode == null ? "" : accountCode.trim();
        boolean postable = ledger.postable(type).stream().anyMatch(a -> a.code().equals(code));
        if (!postable) {
            String nature = switch (type) {
                case EXPENSE -> "an active expense account";
                case INCOME -> "an active revenue account";
            };
            throw new BusinessRuleException("Account " + code + " is not " + nature);
        }
    }
}
