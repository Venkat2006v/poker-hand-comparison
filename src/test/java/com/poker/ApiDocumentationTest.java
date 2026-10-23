package com.poker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiDocumentationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesOpenApiDocumentForTheCompareEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/poker/compare'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/poker/compare'].post.responses['400']").exists());
    }

    @Test
    void publishesExamplesForRequestsAndResponses() throws Exception {
        String compare = "$.paths['/api/poker/compare'].post";
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(compare + ".requestBody.content['application/json'].examples['Tie']").exists())
                .andExpect(jsonPath(compare + ".requestBody.content['application/json'].examples['Invalid: bad card']").exists())
                .andExpect(jsonPath(compare + ".responses['200'].content['application/json'].examples['Tie']").exists())
                .andExpect(jsonPath(compare + ".responses['400'].content['application/json'].examples['Invalid card']").exists());
    }
}
