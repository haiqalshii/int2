package com.int2.company;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CompanyControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired CompanyRepository repository;

    private UUID aegisId;

    @BeforeEach
    void setUp() {
        Company aegis = company("Aegis Cyber Defence", "Kuala Lumpur", "Kuala Lumpur", "SOC");
        aegis.setLatitude(new BigDecimal("3.139000"));
        aegis.setLongitude(new BigDecimal("101.686900"));
        aegisId = repository.save(aegis).getId();

        repository.save(company("Selangor Security Labs", "Shah Alam", "Selangor", "Penetration Testing"));
        repository.save(company("Cyber Response Malaysia", "Petaling Jaya", "Selangor", "Incident Response"));
        repository.flush();
    }

    @Test
    void filtersCompaniesBySearchStateAndService() throws Exception {
        mockMvc.perform(get("/api/companies")
                        .param("search", "security")
                        .param("state", "selangor")
                        .param("service", "Penetration Testing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Selangor Security Labs"))
                .andExpect(jsonPath("$.content[0].services[0]").value("Penetration Testing"));
    }

    @Test
    void paginatesCompanyResults() throws Exception {
        mockMvc.perform(get("/api/companies").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void returnsCompanyDetailsAndLightweightMapRecords() throws Exception {
        mockMvc.perform(get("/api/companies/{id}", aegisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aegis Cyber Defence"))
                .andExpect(jsonPath("$.services[0]").value("SOC"));

        mockMvc.perform(get("/api/companies/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].description").doesNotExist());
    }

    @Test
    void marksAndUnmarksCompanyAsContacted() throws Exception {
        mockMvc.perform(patch("/api/companies/{id}/contacted", aegisId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contacted\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacted").value(true))
                .andExpect(jsonPath("$.contactedAt").isNotEmpty());

        mockMvc.perform(patch("/api/companies/{id}/contacted", aegisId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contacted\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacted").value(false))
                .andExpect(jsonPath("$.contactedAt").doesNotExist());
    }

    @Test
    void returnsUsefulErrorsForMissingCompanyAndInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/companies/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Company not found:")));

        mockMvc.perform(get("/api/companies").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request."));
    }

    private Company company(String name, String city, String state, String service) {
        Company company = new Company(name);
        company.setCity(city);
        company.setState(state);
        company.setNacsaLicence("Cyber Security Service Provider");
        company.setNacsaStatus("approved");
        company.addService(service, "https://licence.nacsa.gov.my/be/licence/licence_holder");
        return company;
    }
}

