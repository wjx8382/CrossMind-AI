package com.crossmind.ai.service;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.crossmind.ai.config.AiProperties;

class BailianLLMClientTests {

    @Test
    void callsOpenAiCompatibleEndpointAndExtractsContent() {
        AiProperties properties = new AiProperties(
                "bailian",
                "test-key",
                "qwen-plus",
                "https://dashscope.aliyuncs.com/compatible-mode/v1",
                Duration.ofSeconds(10),
                Duration.ofSeconds(60)
        );
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BailianLLMClient client = new BailianLLMClient(builder.build(), properties);

        server.expect(requestTo(
                        "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("qwen-plus"))
                .andExpect(jsonPath("$.messages[1].content").value("分析 Portable Blender"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {"message": {"role": "assistant", "content": "  建议差异化进入市场。  "}}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertEquals("建议差异化进入市场。", client.chat("分析 Portable Blender"));
        server.verify();
    }
}
