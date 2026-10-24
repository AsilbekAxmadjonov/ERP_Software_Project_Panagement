package org.example.erp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.erp.modules.identity.application.actions.CreateUser;
import org.example.erp.modules.identity.application.actions.ListUsers;
import org.example.erp.modules.identity.application.actions.UpdateUser;
import org.example.erp.modules.identity.application.commands.CreateUserCommand;
import org.example.erp.modules.identity.application.commands.UpdateUserCommand;
import org.example.erp.modules.identity.application.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** ACC-4: login with roles. Goes through the real JWT filter and SecurityConfig, not mocked users. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:identityaccess;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class IdentityAccessIntegrationTest {
    private static final String PASSWORD = "correct-horse-1";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CreateUser createUser;
    @Autowired UpdateUser updateUser;
    @Autowired ListUsers listUsers;

    @BeforeEach
    void users() {
        ensure("boss", UserRole.CEO);
        ensure("mira", UserRole.MANAGER);
        ensure("aziz", UserRole.ACCOUNTANT);
    }

    private void ensure(String username, UserRole role) {
        if (listUsers.handle().stream().noneMatch(u -> u.username().equals(username)))
            createUser.handle(new CreateUserCommand(username, PASSWORD, null, role));
    }

    private String body(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    private MockHttpServletRequestBuilder login(String username, String password) throws Exception {
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", username, "password", password)));
    }

    private String tokenOf(String username) throws Exception {
        String response = mvc.perform(login(username, PASSWORD)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    private MockHttpServletRequestBuilder as(String username, MockHttpServletRequestBuilder request) throws Exception {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOf(username));
    }

    private String balancedEntry() throws Exception {
        return body(Map.of("date", LocalDate.now().toString(), "reference", "OWN-1", "description", "Owner investment",
                "lines", new Object[]{
                        Map.of("accountCode", "1000", "debit", 500, "credit", 0),
                        Map.of("accountCode", "3000", "debit", 0, "credit", 500)}));
    }

    @Test
    void validCredentialsReturnATokenAndTheUserWithoutAnyPasswordData() throws Exception {
        mvc.perform(login("aziz", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("aziz"))
                .andExpect(jsonPath("$.user.role").value("ACCOUNTANT"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());

        mvc.perform(as("aziz", get("/api/auth/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("aziz"));
    }

    @Test
    void wrongPasswordUnknownUserAndDisabledUserAreAllRejectedTheSameWay() throws Exception {
        mvc.perform(login("aziz", "not-the-password")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        mvc.perform(login("nobody", PASSWORD)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        createUser.handle(new CreateUserCommand("leaver", PASSWORD, null, UserRole.MANAGER));
        long id = listUsers.handle().stream().filter(u -> u.username().equals("leaver")).findFirst().orElseThrow().id();
        updateUser.handle(new UpdateUserCommand(id, null, null, false, null));
        mvc.perform(login("leaver", PASSWORD)).andExpect(status().isUnauthorized());
    }

    @Test
    void blankCredentialsAreAValidationError() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body(Map.of("username", "aziz"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestsWithoutOrWithABadTokenAreUnauthorized() throws Exception {
        mvc.perform(get("/api/partners")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/partners").header(HttpHeaders.AUTHORIZATION, "Bearer forged.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aManagerCanReadTheBooksButCannotPostEntriesOrManageUsers() throws Exception {
        mvc.perform(as("mira", get("/api/accounting/accounts"))).andExpect(status().isOk());
        mvc.perform(as("mira", post("/api/accounting/entries")).contentType(MediaType.APPLICATION_JSON).content(balancedEntry()))
                .andExpect(status().isForbidden());
        mvc.perform(as("mira", get("/api/users"))).andExpect(status().isForbidden());
        mvc.perform(as("mira", post("/api/users")).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("username", "sneaky", "password", PASSWORD, "role", "CEO"))))
                .andExpect(status().isForbidden());
        mvc.perform(as("mira", get("/api/audit"))).andExpect(status().isForbidden());
    }

    @Test
    void anAccountantCanPostEntriesButCannotManageUsers() throws Exception {
        mvc.perform(as("aziz", post("/api/accounting/entries")).contentType(MediaType.APPLICATION_JSON).content(balancedEntry()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy").value("aziz"))
                .andExpect(jsonPath("$.origin").value("MANUAL"));
        mvc.perform(as("aziz", get("/api/users"))).andExpect(status().isForbidden());
        mvc.perform(as("aziz", get("/api/audit"))).andExpect(status().isForbidden());
    }

    @Test
    void onlyTheCeoManagesUsers() throws Exception {
        String created = mvc.perform(as("boss", post("/api/users")).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("username", "newhire", "password", PASSWORD, "fullName", "New Hire", "role", "MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(as("boss", put("/api/users/" + id)).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("role", "ACCOUNTANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ACCOUNTANT"));
        mvc.perform(as("boss", get("/api/users"))).andExpect(status().isOk());
        mvc.perform(login("newhire", PASSWORD)).andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("ACCOUNTANT"));

        mvc.perform(as("boss", post("/api/users")).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("username", "newhire", "password", PASSWORD, "role", "MANAGER"))))
                .andExpect(status().isBadRequest());
        mvc.perform(as("boss", post("/api/users")).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("username", "shortpw", "password", "short", "role", "MANAGER"))))
                .andExpect(status().isBadRequest());
    }
}
