package com.int2.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.int2.candidate.CandidateProfile;
import com.int2.candidate.CandidateRepository;
import com.int2.common.ExternalServiceException;
import com.int2.common.ResourceNotFoundException;
import com.int2.company.Company;
import com.int2.company.CompanyRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailGenerationService {
    private static final Logger log = LoggerFactory.getLogger(EmailGenerationService.class);
    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final GeneratedEmailRepository generatedEmailRepository;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public EmailGenerationService(CompanyRepository companyRepository, CandidateRepository candidateRepository,
            GeneratedEmailRepository generatedEmailRepository, PromptBuilder promptBuilder, LlmClient llmClient,
            ObjectMapper objectMapper) {
        this.companyRepository = companyRepository;
        this.candidateRepository = candidateRepository;
        this.generatedEmailRepository = generatedEmailRepository;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public EmailResponse generate(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
        CandidateProfile candidate = candidateRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new InvalidEmailContextException("No CV has been uploaded yet."));
        if (candidate.getCvText() == null || candidate.getCvText().isBlank()) {
            throw new InvalidEmailContextException("No CV has been uploaded yet.");
        }
        log.info("Generating internship email for company {} using model {}", companyId, llmClient.model());
        String systemPrompt = promptBuilder.systemPrompt();
        String userPrompt = promptBuilder.userPrompt(candidate, company);
        EmailResponse response;
        try {
            response = parseAndValidate(llmClient.generate(systemPrompt, userPrompt));
        } catch (MalformedLlmResponseException firstFailure) {
            log.warn("Malformed LLM response for company {}; retrying once", companyId);
            try {
                response = parseAndValidate(llmClient.generate(systemPrompt, userPrompt));
            } catch (MalformedLlmResponseException secondFailure) {
                throw new ExternalServiceException("The LLM provider returned an invalid email response.", secondFailure);
            }
        }
        GeneratedEmail generated = new GeneratedEmail(company, response.subject(), response.body());
        generated.setModel(llmClient.model());
        generated.setPromptVersion(PromptBuilder.PROMPT_VERSION);
        generatedEmailRepository.save(generated);
        return response;
    }

    private EmailResponse parseAndValidate(String raw) {
        try {
            EmailResponse response = objectMapper.readValue(raw, EmailResponse.class);
            if (response.subject() == null || response.subject().isBlank() || response.subject().length() > 500
                    || response.body() == null || response.body().isBlank() || response.body().length() > 5000) {
                throw new MalformedLlmResponseException("Required email fields are missing or too long.");
            }
            return new EmailResponse(response.subject().trim(), response.body().trim());
        } catch (JsonProcessingException exception) {
            throw new MalformedLlmResponseException("Email response is not valid JSON.", exception);
        }
    }

    private static class MalformedLlmResponseException extends RuntimeException {
        MalformedLlmResponseException(String message) { super(message); }
        MalformedLlmResponseException(String message, Throwable cause) { super(message, cause); }
    }
}

