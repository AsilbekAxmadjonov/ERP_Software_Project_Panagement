package org.example.erp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.erp.modules.identity.application.actions.CreateUser;
import org.example.erp.modules.identity.application.actions.UpdateUser;
import org.example.erp.modules.identity.application.commands.CreateUserCommand;
import org.example.erp.modules.identity.application.commands.UpdateUserCommand;
import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.modules.inventory.application.actions.AdjustStock;
import org.example.erp.modules.inventory.application.actions.CreateProduct;
import org.example.erp.modules.inventory.application.commands.AdjustStockCommand;
import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.example.erp.modules.partners.application.actions.CreatePartner;
import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.example.erp.modules.sales.application.actions.IssueInvoice;
import org.example.erp.modules.sales.application.commands.IssueInvoiceCommand;
import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.platform.BaseIntegrationEvent;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REP-5: business changes leave an audit trail with the acting user, readable by the CEO only. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:audit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "accountant", roles = "ACCOUNTANT")
class AuditIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CreatePartner createPartner;
    @Autowired CreateProduct createProduct;
    @Autowired AdjustStock adjustStock;
    @Autowired IssueInvoice issueInvoice;
    @Autowired CreateUser createUser;
    @Autowired UpdateUser updateUser;
    @Autowired IntegrationEventPublisher events;
    @Autowired TransactionRunner tx;

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private List<JsonNode> audit(MockHttpServletRequestBuilder request) throws Exception {
        String body = mvc.perform(request.with(user("boss").roles("CEO"))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<JsonNode> rows = new ArrayList<>();
        json.readTree(body).forEach(rows::add);
        return rows;
    }

    private List<JsonNode> audit(String action) throws Exception {
        return audit(get("/api/audit").param("action", action));
    }

    private JsonNode single(List<JsonNode> rows, String field, String value) {
        List<JsonNode> matches = rows.stream().filter(r -> value.equals(r.path(field).asText())).toList();
        assertEquals(1, matches.size(), () -> "expected one row with " + field + "=" + value + " in " + rows);
        return matches.getFirst();
    }

    @Test
    void issuingAnInvoiceRecordsWhoDidItAndWhatWasIssued() throws Exception {
        var customer = createPartner.handle(new SavePartnerCommand("Audit Bazaar", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var flour = createProduct.handle(new SaveProductCommand("AU-1", "Audit flour", UnitOfMeasure.KG, n("2200"), true, null));
        adjustStock.handle(new AdjustStockCommand(flour.id(), n("10"), n("2000"), "Opening stock"));

        InvoiceDTO invoice = issueInvoice.handle(new IssueInvoiceCommand(customer.id(), null,
                List.of(new IssueInvoiceCommand.Item(flour.id(), n("4"), null))));

        JsonNode row = single(audit("sales.invoice-issued"), "subject", invoice.number());
        assertEquals("accountant", row.get("actor").asText());
        assertEquals("sales.invoice-issued", row.get("action").asText());
        assertEquals("Invoice issued " + invoice.number(), row.get("summary").asText());
        assertEquals("Audit Bazaar", row.get("payload").get("customerName").asText());
        assertEquals(0, n("8800").compareTo(new BigDecimal(row.get("payload").get("total").asText())));
        assertFalse(row.get("occurredAt").asText().isBlank());

        JsonNode ledger = single(audit("accounting.journal-entry-posted"), "subject", invoice.number());
        assertEquals("SALE", ledger.get("payload").get("origin").asText());
        assertEquals("accountant", single(audit("partners"), "subject", "Audit Bazaar").get("actor").asText());
        assertEquals("accountant", single(audit("inventory.product-created"), "subject", "AU-1").get("actor").asText());
    }

    @Test
    void aFailedBusinessActionLeavesNoAuditEntry() throws Exception {
        var customer = createPartner.handle(new SavePartnerCommand("Rollback Shop", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var sugar = createProduct.handle(new SaveProductCommand("AU-2", "Audit sugar", UnitOfMeasure.KG, n("1800"), true, null));
        int before = audit("sales.invoice-issued").size();

        assertThrows(BusinessRuleException.class, () -> issueInvoice.handle(new IssueInvoiceCommand(customer.id(), null,
                List.of(new IssueInvoiceCommand.Item(sugar.id(), n("1"), null)))));

        assertEquals(before, audit("sales.invoice-issued").size());
    }

    @Test
    void loginsAreAuditedUnderTheAttemptedUsernameWithoutThePassword() throws Exception {
        createUser.handle(new CreateUserCommand("dilnoza", "super-secret-pw", "Dilnoza", UserRole.MANAGER));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", "dilnoza", "password", "wrong-password!")))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", "dilnoza", "password", "super-secret-pw")))).andExpect(status().isOk());

        List<JsonNode> failed = audit(get("/api/audit").param("action", "identity.login-failed").param("actor", "dilnoza"));
        assertEquals(1, failed.size());
        assertEquals("wrong-password", failed.getFirst().get("payload").get("reason").asText());
        assertEquals(1, audit(get("/api/audit").param("action", "identity.login-succeeded").param("actor", "dilnoza")).size());

        String everything = mvc.perform(get("/api/audit").with(user("boss").roles("CEO"))).andReturn().getResponse().getContentAsString();
        assertFalse(everything.contains("super-secret-pw"));
        assertFalse(everything.contains("wrong-password!"));
        assertFalse(everything.contains("$2a$"), "no password hashes");
    }

    @Test
    void userChangesAreAuditedIncludingRoleChangesAndDisabling() throws Exception {
        var user = createUser.handle(new CreateUserCommand("temp.worker", "temporary-pw", null, UserRole.MANAGER));
        updateUser.handle(new UpdateUserCommand(user.id(), "Temp Worker", UserRole.ACCOUNTANT, false, "another-password"));

        assertEquals("accountant", single(audit("identity.user-created"), "subject", "temp.worker").get("actor").asText());
        JsonNode updated = single(audit("identity.user-updated"), "subject", "temp.worker");
        assertTrue(updated.get("payload").get("passwordChanged").asBoolean());
        JsonNode role = single(audit("identity.user-role-changed"), "subject", "temp.worker");
        assertEquals("MANAGER", role.get("payload").get("previousRole").asText());
        assertEquals("ACCOUNTANT", role.get("payload").get("role").asText());
        single(audit("identity.user-disabled"), "subject", "temp.worker");
    }

    /** A module written later (expenses, payroll, ...) is audited just by publishing its events. */
    static final class SomethingNewHappened extends BaseIntegrationEvent {
        SomethingNewHappened() {
            super(null, null);
        }

        @Override
        public String eventName() {
            return "futuremodule.thing-approved";
        }

        @Override
        public Map<String, Object> payload() {
            return Map.of("voucherNumber", "FM-42", "approverToken", "must-not-be-stored");
        }
    }

    @Test
    void eventsFromAnyModuleAreRecordedWithoutChangesToTheAuditModule() throws Exception {
        tx.run(() -> events.publish(new SomethingNewHappened()));

        JsonNode row = single(audit("futuremodule"), "subject", "FM-42");
        assertEquals("Thing approved FM-42", row.get("summary").asText());
        assertFalse(row.get("payload").has("approverToken"));
    }

    @Test
    void filtersByActorAndDateAndKeepsNewestFirst() throws Exception {
        createPartner.handle(new SavePartnerCommand("First Co", PartnerKind.SUPPLIER, null, null, null, null, null, true));
        createPartner.handle(new SavePartnerCommand("Second Co", PartnerKind.SUPPLIER, null, null, null, null, null, true));

        List<JsonNode> rows = audit(get("/api/audit").param("action", "partners.partner-created").param("actor", "ACCOUNTANT")
                .param("from", LocalDate.now().minusDays(1).toString()).param("to", LocalDate.now().toString()));
        List<String> subjects = rows.stream().map(r -> r.get("subject").asText()).toList();
        assertTrue(subjects.indexOf("Second Co") < subjects.indexOf("First Co"), "newest first: " + subjects);
        assertTrue(subjects.indexOf("First Co") >= 0);

        assertTrue(audit(get("/api/audit").param("actor", "somebody-else")).isEmpty());
        assertTrue(audit(get("/api/audit").param("from", LocalDate.now().plusDays(1).toString())).isEmpty());
        assertEquals(1, audit(get("/api/audit").param("limit", "1")).size());
        mvc.perform(get("/api/audit").param("from", "2026-02-02").param("to", "2026-02-01").with(user("boss").roles("CEO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onlyTheCeoCanReadTheAuditLog() throws Exception {
        mvc.perform(get("/api/audit")).andExpect(status().isForbidden());
        mvc.perform(get("/api/audit").with(user("mira").roles("MANAGER"))).andExpect(status().isForbidden());
    }
}
