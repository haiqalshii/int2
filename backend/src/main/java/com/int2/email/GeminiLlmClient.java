package com.int2.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.int2.common.ExternalServiceException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "app.llm.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiLlmClient implements LlmClient {
    private final String apiKey;
    private final String model;
    private final RestClient client;

    @Autowired
    public GeminiLlmClient(
            @Value("${app.llm.gemini.api-key:}") String apiKey,
            @Value("${app.llm.gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
            @Value("${app.llm.gemini.model:gemini-2.5-flash}") String model,
            @Value("${app.llm.timeout-seconds:45}") int timeoutSeconds) {
        this(apiKey, model, restClientBuilder(baseUrl, timeoutSeconds));
    }

    GeminiLlmClient(String apiKey, String model, RestClient.Builder builder) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = builder.build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ExternalServiceException("The Gemini API is not configured. Set GEMINI_API_KEY.");
        }
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "subject", Map.of("type", "string", "description", "Concise email subject"),
                        "body", Map.of("type", "string", "description", "Complete plain-text email body")),
                "required", List.of("subject", "body"),
                "additionalProperties", false);
        Map<String, Object> request = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", schema,
                        "maxOutputTokens", 1000,
                        "temperature", 0.3));
        try {
            JsonNode response = client.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            return outputText(response);
        } catch (RestClientException exception) {
            throw new ExternalServiceException("Unable to generate the email with Gemini right now. Please try again.", exception);
        }
    }

    private String outputText(JsonNode response) {
        if (response == null) throw new ExternalServiceException("Gemini returned an empty response.");
        JsonNode candidates = response.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray()) {
                for (JsonNode part : parts) {
                    if (part.path("text").isTextual()) return part.path("text").asText();
                }
            }
        }
        String blockReason = response.path("promptFeedback").path("blockReason").asText();
        if (StringUtils.hasText(blockReason)) {
            throw new ExternalServiceException("Gemini declined this request: " + blockReason + ".");
        }
        throw new ExternalServiceException("Gemini returned no email content.");
    }

    @Override
    public String model() { return model; }

    private static RestClient.Builder restClientBuilder(String baseUrl, int timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)));
        return RestClient.builder()
                .baseUrl(baseUrl.replaceAll("/+$", ""))
                .requestFactory(requestFactory);
    }
}
