package com.int2.email;

import static org.assertj.core.api.Assertions.assertThat;

import com.int2.candidate.CandidateProfile;
import com.int2.company.Company;
import org.junit.jupiter.api.Test;

class PromptBuilderTest {
    private final PromptBuilder builder = new PromptBuilder();

    @Test
    void buildsGroundedPromptFromCandidateAndCompany() {
        CandidateProfile candidate = new CandidateProfile("Hanim", "hanim@example.com");
        candidate.setCvText("Cybersecurity student with a network monitoring project.");
        Company company = new Company("ABC Cybersecurity Sdn Bhd");
        company.setCity("Kuala Lumpur");
        company.setDescription("A managed security provider.");
        company.addService("SOC", "https://example.com/services");

        String prompt = builder.userPrompt(candidate, company);
        assertThat(prompt).contains("Hanim", "network monitoring project", "ABC Cybersecurity Sdn Bhd", "SOC", "Kuala Lumpur");
        assertThat(builder.systemPrompt()).contains("Use ONLY information supported", "Never fabricate", "100-180 words");
    }
}

