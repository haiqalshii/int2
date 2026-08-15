package com.int2.email;

public interface LlmClient {
    String generate(String systemPrompt, String userPrompt);
    String model();
}

