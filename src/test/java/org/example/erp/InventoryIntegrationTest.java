package org.example.erp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.erp.modules.inventory.application.actions.FindProduct;
import org.example.erp.modules.inventory.application.actions.IssueStock;
import org.example.erp.modules.inventory.application.actions.ListStockMovements;
import org.example.erp.modules.inventory.application.actions.ReceiveStock;
import org.example.erp.modules.inventory.application.commands.IssueStockCommand;
import org.example.erp.modules.inventory.application.commands.ReceiveStockCommand;
import org.example.erp.modules.inventory.application.dto.ProductDTO;
import org.example.erp.modules.inventory.application.dto.StockMovementDTO;
import org.example.erp.modules.inventory.application.enums.MovementKind;
import org.example.erp.modules.inventory.application.enums.StockReceipt;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** INV-1 product list, INV-2 stock quantity and value, INV-3 movement history. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:inventory;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "manager", roles = "MANAGER")
class InventoryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ReceiveStock receiveStock;
    @Autowired IssueStock issueStock;
    @Autowired FindProduct findProduct;
    @Autowired ListStockMovements listMovements;

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private Map<String, Object> product(String code, String name, UnitOfMeasure unit) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("name", name);
        body.put("unit", unit == null ? null : unit.name());
        body.put("salePrice", 2200);
        return body;
    }

    private long createProduct(String code, String name, UnitOfMeasure unit) throws Exception {
        String response = mvc.perform(post("/api/inventory/products").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(product(code, name, unit))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    @Test
    void productsHaveACodeNameAndUnitAndStartWithNoStock() throws Exception {
        long flour = createProduct("FL-1", "Flour", UnitOfMeasure.KG);
        createProduct("BX-1", "Gift box", UnitOfMeasure.PCS);

        mvc.perform(get("/api/inventory/products/" + flour))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("FL-1"))
                .andExpect(jsonPath("$.name").value("Flour"))
                .andExpect(jsonPath("$.unit").value("KG"))
                .andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.stockValue").value(0))
                .andExpect(jsonPath("$.active").value(true));
        mvc.perform(get("/api/inventory/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'BX-1')].unit").value("PCS"));
    }

    @Test
    void codesAreUniqueAndCodeNameUnitAreRequired() throws Exception {
        createProduct("UNI-1", "Sugar", UnitOfMeasure.KG);
        mvc.perform(post("/api/inventory/products").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(product(" UNI-1 ", "Other sugar", UnitOfMeasure.KG))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product code already exists"));
        mvc.perform(post("/api/inventory/products").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(product("NU-1", "No unit", null))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/inventory/products").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(product("", "No code", UnitOfMeasure.KG))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/inventory/products").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(product("NN-1", " ", UnitOfMeasure.KG))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stockValueFollowsTheWeightedAverageCostThroughReceiptsAndIssues() throws Exception {
        long rice = createProduct("RC-1", "Rice", UnitOfMeasure.KG);
        LocalDate day = LocalDate.of(2026, 10, 1);

        receiveStock.handle(new ReceiveStockCommand(rice, n("100"), n("2000"), StockReceipt.PURCHASE, "PO-1", day));
        receiveStock.handle(new ReceiveStockCommand(rice, n("50"), n("2300"), StockReceipt.PURCHASE, "PO-2", day));
        ProductDTO afterReceipts = findProduct.handle(rice);
        assertEquals(0, n("150").compareTo(afterReceipts.quantity()));
        assertEquals(0, n("2100").compareTo(afterReceipts.averageCost()));
        assertEquals(0, n("315000.00").compareTo(afterReceipts.stockValue()));

        BigDecimal issuedAt = issueStock.handle(new IssueStockCommand(rice, n("60"), "INV-1", day.plusDays(1)));
        assertEquals(0, n("2100").compareTo(issuedAt), "goods leave stock at the average cost");
        ProductDTO afterIssue = findProduct.handle(rice);
        assertEquals(0, n("90").compareTo(afterIssue.quantity()));
        assertEquals(0, n("189000.00").compareTo(afterIssue.stockValue()));

        mvc.perform(get("/api/inventory/products/" + rice))
                .andExpect(jsonPath("$.quantity").value(90))
                .andExpect(jsonPath("$.stockValue").value(189000));
    }

    @Test
    void issuingMoreThanOnHandFailsAndLeavesStockUntouched() throws Exception {
        long oil = createProduct("OL-1", "Oil", UnitOfMeasure.PCS);
        receiveStock.handle(new ReceiveStockCommand(oil, n("5"), n("12000"), StockReceipt.PURCHASE, "PO-9", LocalDate.now()));

        assertThrows(BusinessRuleException.class, () -> issueStock.handle(new IssueStockCommand(oil, n("6"), "INV-9", LocalDate.now())));

        assertEquals(0, n("5").compareTo(findProduct.handle(oil).quantity()));
        assertEquals(1, listMovements.handle(oil).size());
    }

    @Test
    void everyReceiptAndIssueIsKeptInTheMovementHistoryNewestFirst() throws Exception {
        long salt = createProduct("SL-1", "Salt", UnitOfMeasure.KG);
        LocalDate day = LocalDate.of(2026, 9, 15);
        receiveStock.handle(new ReceiveStockCommand(salt, n("40"), n("500"), StockReceipt.PURCHASE, "PO-77", day));
        issueStock.handle(new IssueStockCommand(salt, n("15"), "INV-77", day.plusDays(2)));

        List<StockMovementDTO> history = listMovements.handle(salt);
        assertEquals(2, history.size());
        StockMovementDTO issue = history.get(0);
        StockMovementDTO receipt = history.get(1);
        assertEquals(MovementKind.SALE, issue.type());
        assertEquals(0, n("-15").compareTo(issue.quantity()));
        assertEquals("INV-77", issue.reference());
        assertEquals(day.plusDays(2), issue.date());
        assertEquals(MovementKind.PURCHASE, receipt.type());
        assertEquals(0, n("40").compareTo(receipt.quantity()));
        assertEquals(0, n("500").compareTo(receipt.unitCost()));
        assertEquals("PO-77", receipt.reference());

        mvc.perform(get("/api/inventory/movements").param("productId", String.valueOf(salt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("SALE"))
                .andExpect(jsonPath("$[1].type").value("PURCHASE"))
                .andExpect(jsonPath("$[1].productName").value("Salt"));
    }
}
