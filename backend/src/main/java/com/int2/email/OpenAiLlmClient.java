package com.int2.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.int2.common.ExternalServiceException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "openai")
public class OpenAiLlmClient implements LlmClient {
    private final String apiKey;
    private final String model;
    private final RestClient client;

    public OpenAiLlmClient(
            @Value("${app.llm.api-key:}") String apiKey,
            @Value("${app.llm.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.llm.model:gpt-5-mini}") String model,
            @Value("${app.llm.timeout-seconds:45}") int timeoutSeconds) {
        this.apiKey = apiKey;
        this.model = model;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)));
        this.client = RestClient.builder().baseUrl(baseUrl.replaceAll("/+$", ""))
                .requestFactory(requestFactory).build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(apiKey)) throw new ExternalServiceException("The LLM provider is not configured.");
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "subject", Map.of("type", "string", "maxLength", 500),
                        "body", Map.of("type", "string", "maxLength", 5000)),
                "required", List.of("subject", "body"),
                "additionalProperties", false);
        Map<String, Object> request = Map.of(
                "model", model,
                "input", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "text", Map.of("format", Map.of(
                        "type", "json_schema", "name", "internship_email", "strict", true, "schema", schema)),
                "max_output_tokens", 1000);
        try {
            JsonNode response = client.post().uri("/responses")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(request).retrieve().body(JsonNode.class);
            return outputText(response);
        } catch (RestClientException exception) {
            throw new ExternalServiceException("Unable to generate the email right now. Please try again.", exception);
        }
    }

    private String outputText(JsonNode response) {
        if (response == null) throw new ExternalServiceException("The LLM provider returned an empty response.");
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("refusal".equals(content.path("type").asText())) {
                    throw new ExternalServiceException("The LLM provider declined this request.");
                }
                if ("output_text".equals(content.path("type").asText()) && content.path("text").isTextual()) {
                    return content.path("text").asText();
                }
            }
        }
        throw new ExternalServiceException("The LLM provider returned no email content.");
    }

    @Override public String model() { return model; }
}
