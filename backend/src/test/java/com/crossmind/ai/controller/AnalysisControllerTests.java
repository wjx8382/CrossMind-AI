package com.crossmind.ai.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsAndRetrievesAnalysis() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"product":"Portable Blender","market":"USA Amazon"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.analysisId", notNullValue()))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andReturn();

        JsonNode response = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String analysisId = response.get("analysisId").asText();

        JsonNode completed = awaitCompleted(analysisId);
        if (!"COMPLETED".equals(completed.get("status").asText())) {
            fail("Agent workflow did not complete: " + completed);
        }

        mockMvc.perform(get("/api/analysis/{id}", analysisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("Portable Blender"))
                .andExpect(jsonPath("$.market").value("USA Amazon"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.score").value(86))
                .andExpect(jsonPath("$.trend").value("增长"))
                .andExpect(jsonPath("$.competition").value("中等"))
                .andExpect(jsonPath("$.painPoints.length()").value(4))
                .andExpect(jsonPath("$.productSuggestions.length()").value(3))
                .andExpect(jsonPath("$.agentTasks.length()").value(4))
                .andExpect(jsonPath("$.agentTasks[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.agentTasks[0].result.summary", notNullValue()))
                .andExpect(jsonPath("$.agentTasks[3].status").value("COMPLETED"));
    }

    @Test
    void exposesSafeAiRuntimeMetadata() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.ai.provider").value("mock"))
                .andExpect(jsonPath("$.ai.model").value("deterministic-demo"))
                .andExpect(jsonPath("$.ai.live").value(false));
    }

    @Test
    void rejectsBlankProduct() throws Exception {
        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"product":" ","market":"USA Amazon"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.product", notNullValue()));
    }

    private JsonNode awaitCompleted(String analysisId) throws Exception {
        JsonNode latest = null;
        for (int attempt = 0; attempt < 100; attempt++) {
            MvcResult result = mockMvc.perform(get("/api/analysis/{id}", analysisId))
                    .andExpect(status().isOk())
                    .andReturn();
            latest = objectMapper.readTree(result.getResponse().getContentAsString());
            if (!"RUNNING".equals(latest.get("status").asText())) {
                return latest;
            }
            Thread.sleep(20);
        }
        return latest;
    }
}
