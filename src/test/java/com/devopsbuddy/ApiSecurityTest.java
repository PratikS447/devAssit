package com.devopsbuddy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "devopsbuddy.api.key=test-only-api-key-that-is-at-least-32-chars",
        "devopsbuddy.execution.enabled=false"
})
@AutoConfigureMockMvc
class ApiSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiRequiresAnApiKey() throws Exception {
        mockMvc.perform(get("/api/commands"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiAcceptsTheConfiguredApiKey() throws Exception {
        mockMvc.perform(get("/api/commands")
                        .header("X-API-Key", "test-only-api-key-that-is-at-least-32-chars"))
                .andExpect(status().isOk());
    }

    @Test
    void healthCheckDoesNotRequireAnApiKey() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void askRejectsBlankPromptsBeforeCallingTheModel() throws Exception {
        mockMvc.perform(post("/api/ask")
                        .header("X-API-Key", "test-only-api-key-that-is-at-least-32-chars")
                        .contentType(APPLICATION_JSON)
                        .content("{\"prompt\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }
}
