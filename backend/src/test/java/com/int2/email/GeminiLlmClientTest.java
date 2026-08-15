package com.int2.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

class GeminiLlmClientTest {
    @Test
    void sendsStructuredPromptAndExtractsGeminiText() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiLlmClient client = new GeminiLlmClient("test-key", "gemini-test", builder);

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-test:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("system_instruction"),
                        org.hamcrest.Matchers.containsString("system rules"),
                        org.hamcrest.Matchers.containsString("candidate context"),
                        org.hamcrest.Matchers.containsString("responseMimeType"),
                        org.hamcrest.Matchers.containsString("responseSchema"))))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"subject\\":\\"Inquiry\\",\\"body\\":\\"Dear Hiring Team,\\"}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.generate("system rules", "candidate context"))
                .isEqualTo("{\"subject\":\"Inquiry\",\"body\":\"Dear Hiring Team,\"}");
        assertThat(client.model()).isEqualTo("gemini-test");
        server.verify();
    }
}
