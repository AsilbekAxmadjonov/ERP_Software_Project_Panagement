package org.example.erp.modules.sales.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.erp.modules.sales.application.actions.*;
import org.example.erp.modules.sales.application.commands.IssueCreditNoteCommand;
import org.example.erp.modules.sales.application.commands.IssueInvoiceCommand;
import org.example.erp.modules.sales.application.commands.RecordPaymentCommand;
import org.example.erp.modules.sales.application.dto.CreditNoteDTO;
import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.application.dto.PaymentDTO;
import org.example.erp.modules.sales.application.dto.SalesReportRowDTO;
import org.example.erp.modules.sales.application.enums.PaymentChannel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
class SalesController {
    private final IssueInvoice issueInvoice;
    private final RecordPayment recordPayment;
    private final ListInvoices listInvoices;
    private final ListPayments listPayments;
    private final GetSalesReport salesReport;
    private final FindInvoice findInvoice;
    private final IssueCreditNote issueCreditNote;
    private final ListCreditNotes listCreditNotes;

    SalesController(IssueInvoice issueInvoice, RecordPayment recordPayment, ListInvoices listInvoices,
                    ListPayments listPayments, GetSalesReport salesReport, FindInvoice findInvoice,
                    IssueCreditNote issueCreditNote, ListCreditNotes listCreditNotes) {
        this.issueInvoice = issueInvoice;
        this.recordPayment = recordPayment;
        this.listInvoices = listInvoices;
        this.listPayments = listPayments;
        this.salesReport = salesReport;
        this.findInvoice = findInvoice;
        this.issueCreditNote = issueCreditNote;
        this.listCreditNotes = listCreditNotes;
    }

    record ItemRequest(@NotNull Long productId, @NotNull BigDecimal quantity, BigDecimal unitPrice) {}

    record InvoiceRequest(@NotNull Long customerId, LocalDate date, @NotEmpty @Valid List<ItemRequest> items) {}

    record PaymentRequest(@NotNull Long customerId, Long invoiceId, @NotNull BigDecimal amount,
                          @NotNull PaymentChannel method, LocalDate date, String note) {}

    record ReturnItemRequest(@NotNull Long productId, @NotNull BigDecimal quantity) {}

    record CreditNoteRequest(LocalDate date, String reason, @NotEmpty @Valid List<ReturnItemRequest> items) {}

    @GetMapping("/invoices")
    List<InvoiceDTO> invoices() {
        return listInvoices.handle();
    }

    @PostMapping("/invoices")
    InvoiceDTO issue(@RequestBody @Valid InvoiceRequest r) {
        return issueInvoice.handle(new IssueInvoiceCommand(r.customerId(), r.date(),
                r.items().stream().map(i -> new IssueInvoiceCommand.Item(i.productId(), i.quantity(), i.unitPrice())).toList()));
    }

    @GetMapping("/invoices/{id}")
    InvoiceDTO invoice(@PathVariable long id) {
        return findInvoice.handle(id);
    }

    @PostMapping("/invoices/{id}/credit-notes")
    CreditNoteDTO creditNote(@PathVariable long id, @RequestBody @Valid CreditNoteRequest r) {
        return issueCreditNote.handle(new IssueCreditNoteCommand(id, r.date(), r.reason(),
                r.items().stream().map(i -> new IssueCreditNoteCommand.Item(i.productId(), i.quantity())).toList()));
    }

    @GetMapping("/credit-notes")
    List<CreditNoteDTO> creditNotes(@RequestParam(required = false) Long invoiceId) {
        return listCreditNotes.handle(invoiceId);
    }

    @GetMapping("/payments")
    List<PaymentDTO> payments() {
        return listPayments.handle();
    }

    @PostMapping("/payments")
    PaymentDTO pay(@RequestBody @Valid PaymentRequest r) {
        return recordPayment.handle(new RecordPaymentCommand(r.customerId(), r.invoiceId(), r.amount(), r.method(), r.date(), r.note()));
    }

    @GetMapping("/report")
    List<SalesReportRowDTO> report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                   @RequestParam(required = false) Long customerId,
                                   @RequestParam(required = false) Long productId) {
        return salesReport.handle(from, to, customerId, productId);
    }
}
