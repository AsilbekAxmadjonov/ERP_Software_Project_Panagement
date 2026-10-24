package org.example.erp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** SAL-1 / PUR-1: one partner directory where the type decides whether a contact is a customer, a supplier or both. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:partners;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "manager", roles = "MANAGER")
class PartnersIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Map<String, Object> partner(String name, PartnerKind type) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("type", type == null ? null : type.name());
        body.put("phone", "+998 90 123 45 67");
        return body;
    }

    private String create(Map<String, Object> body) throws Exception {
        return mvc.perform(post("/api/partners").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test
    void theTypeDecidesTheCustomerAndSupplierFlags() throws Exception {
        mvc.perform(post("/api/partners").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(partner("  Silk Road Market  ", PartnerKind.CUSTOMER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Silk Road Market"))
                .andExpect(jsonPath("$.customer").value(true))
                .andExpect(jsonPath("$.supplier").value(false))
                .andExpect(jsonPath("$.active").value(true));
        mvc.perform(post("/api/partners").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(partner("Fergana Mill", PartnerKind.SUPPLIER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer").value(false))
                .andExpect(jsonPath("$.supplier").value(true));
        mvc.perform(post("/api/partners").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(partner("Trading House", PartnerKind.BOTH))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer").value(true))
                .andExpect(jsonPath("$.supplier").value(true));
    }

    @Test
    void nameAndTypeAreRequired() throws Exception {
        mvc.perform(post("/api/partners").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(partner(" ", PartnerKind.CUSTOMER))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("name")));
        mvc.perform(post("/api/partners").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(partner("No Type", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("type")));
    }

    @Test
    void updatingChangesTheTypeFlagsAndActiveState() throws Exception {
        long id = json.readTree(create(partner("Bazaar", PartnerKind.CUSTOMER))).get("id").asLong();

        Map<String, Object> update = partner("Bazaar Wholesale", PartnerKind.BOTH);
        update.put("active", false);
        update.put("taxId", "301234567");
        mvc.perform(put("/api/partners/" + id).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bazaar Wholesale"))
                .andExpect(jsonPath("$.supplier").value(true))
                .andExpect(jsonPath("$.customer").value(true))
                .andExpect(jsonPath("$.active").value(false));

        mvc.perform(get("/api/partners/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("BOTH"))
                .andExpect(jsonPath("$.taxId").value("301234567"));
    }

    @Test
    void updateValidatesAndRejectsUnknownPartners() throws Exception {
        long id = json.readTree(create(partner("Valid Co", PartnerKind.SUPPLIER))).get("id").asLong();
        mvc.perform(put("/api/partners/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(partner("", PartnerKind.SUPPLIER))))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/partners/999999").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(partner("Ghost", PartnerKind.SUPPLIER))))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/partners/999999")).andExpect(status().isNotFound());
    }
}
