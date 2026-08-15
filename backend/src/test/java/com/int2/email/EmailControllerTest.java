package com.int2.email;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.int2.candidate.CandidateProfile;
import com.int2.candidate.CandidateRepository;
import com.int2.company.Company;
import com.int2.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmailControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired CompanyRepository companyRepository;
    @Autowired CandidateRepository candidateRepository;
    @Autowired GeneratedEmailRepository generatedEmailRepository;
    @MockitoBean LlmClient llmClient;

    private Company company;

    @BeforeEach
    void setUp() {
        generatedEmailRepository.deleteAll();
        candidateRepository.deleteAll();
        companyRepository.deleteAll();
        company = new Company("ABC Security");
        company.addService("SOC", "https://example.com/services");
        company = companyRepository.saveAndFlush(company);
        when(llmClient.model()).thenReturn("test-model");
    }

    @Test
    void generatesValidEmailAndStoresHistory() throws Exception {
        saveCandidate();
        when(llmClient.generate(anyString(), anyString())).thenReturn("{\"subject\":\"Internship inquiry\",\"body\":\"Dear Hiring Team,\\n\\nI am writing to ask about internship opportunities.\"}");

        mockMvc.perform(post("/api/email/generate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyId\":\"" + company.getId() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Internship inquiry"))
                .andExpect(jsonPath("$.body").value(org.hamcrest.Matchers.startsWith("Dear Hiring Team")));
        org.assertj.core.api.Assertions.assertThat(generatedEmailRepository.count()).isEqualTo(1);
    }

    @Test
    void retriesMalformedResponseOnce() throws Exception {
        saveCandidate();
        when(llmClient.generate(anyString(), anyString())).thenReturn("not-json", "{\"subject\":\"Inquiry\",\"body\":\"Dear Hiring Team, valid body.\"}");
        mockMvc.perform(post("/api/email/generate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyId\":\"" + company.getId() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subject").value("Inquiry"));
        verify(llmClient, times(2)).generate(anyString(), anyString());
    }

    @Test
    void rejectsGenerationWithoutCv() throws Exception {
        mockMvc.perform(post("/api/email/generate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyId\":\"" + company.getId() + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("No CV has been uploaded yet."));
    }

    private void saveCandidate() {
        CandidateProfile candidate = new CandidateProfile("Hanim", "hanim@example.com");
        candidate.setCvText("Cybersecurity student with Java experience.");
        candidate.setCvFilePath("candidate/cv.pdf");
        candidateRepository.saveAndFlush(candidate);
    }
}

