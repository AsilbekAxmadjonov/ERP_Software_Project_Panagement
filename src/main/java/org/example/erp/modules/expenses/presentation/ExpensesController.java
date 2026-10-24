package org.example.erp.modules.expenses.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.erp.modules.expenses.application.actions.*;
import org.example.erp.modules.expenses.application.commands.CreateCategoryCommand;
import org.example.erp.modules.expenses.application.commands.RecordCashEntryCommand;
import org.example.erp.modules.expenses.application.commands.UpdateCategoryCommand;
import org.example.erp.modules.expenses.application.dto.CashEntryDTO;
import org.example.erp.modules.expenses.application.dto.CategoryDTO;
import org.example.erp.modules.expenses.application.dto.CategoryReportDTO;
import org.example.erp.modules.expenses.application.dto.LedgerAccountDTO;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
class ExpensesController {
    private final ListCategories listCategories;
    private final CreateCategory createCategory;
    private final UpdateCategory updateCategory;
    private final ListLedgerAccounts listAccounts;
    private final RecordCashEntry recordEntry;
    private final ListCashEntries listEntries;
    private final FindCashEntry findEntry;
    private final CancelCashEntry cancelEntry;
    private final GetCategoryReport report;

    ExpensesController(ListCategories listCategories, CreateCategory createCategory, UpdateCategory updateCategory,
                       ListLedgerAccounts listAccounts, RecordCashEntry recordEntry, ListCashEntries listEntries,
                       FindCashEntry findEntry, CancelCashEntry cancelEntry, GetCategoryReport report) {
        this.listCategories = listCategories;
        this.createCategory = createCategory;
        this.updateCategory = updateCategory;
        this.listAccounts = listAccounts;
        this.recordEntry = recordEntry;
        this.listEntries = listEntries;
        this.findEntry = findEntry;
        this.cancelEntry = cancelEntry;
        this.report = report;
    }

    record CreateCategoryRequest(@NotBlank String name, @NotNull EntryType type, @NotBlank String accountCode) {}

    record UpdateCategoryRequest(@NotBlank String name, @NotBlank String accountCode, @NotNull Boolean active) {}

    record EntryRequest(@NotNull EntryType type, @NotNull Long categoryId, LocalDate date, @NotNull BigDecimal amount,
                        @NotNull PaymentChannel method, String description) {}

    record CancelRequest(LocalDate date) {}

    /** type is optional: all categories are returned without it. */
    @GetMapping("/categories")
    List<CategoryDTO> categories(@RequestParam(required = false) EntryType type) {
        return listCategories.handle(type);
    }

    @PostMapping("/categories")
    CategoryDTO createCategory(@RequestBody @Valid CreateCategoryRequest r) {
        return createCategory.handle(new CreateCategoryCommand(r.name(), r.type(), r.accountCode()));
    }

    @PutMapping("/categories/{id}")
    CategoryDTO updateCategory(@PathVariable long id, @RequestBody @Valid UpdateCategoryRequest r) {
        return updateCategory.handle(id, new UpdateCategoryCommand(r.name(), r.accountCode(), r.active()));
    }

    @GetMapping("/accounts")
    List<LedgerAccountDTO> accounts(@RequestParam EntryType type) {
        return listAccounts.handle(type);
    }

    @GetMapping("/entries")
    List<CashEntryDTO> entries(@RequestParam EntryType type) {
        return listEntries.handle(type);
    }

    @PostMapping("/entries")
    CashEntryDTO record(@RequestBody @Valid EntryRequest r) {
        return recordEntry.handle(new RecordCashEntryCommand(r.type(), r.categoryId(), r.date(), r.amount(), r.method(), r.description()));
    }

    @GetMapping("/entries/{id}")
    CashEntryDTO entry(@PathVariable long id) {
        return findEntry.handle(id);
    }

    /** Body is optional: without a date the entry is cancelled (and reversed in the ledger) today. */
    @PostMapping("/entries/{id}/cancel")
    CashEntryDTO cancel(@PathVariable long id, @RequestBody(required = false) CancelRequest r) {
        return cancelEntry.handle(id, r == null ? null : r.date());
    }

    @GetMapping("/report")
    CategoryReportDTO report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return report.handle(from, to);
    }
}
