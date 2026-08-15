package com.int2.company;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CompanyRepositoryTest {
    @Autowired CompanyRepository repository;

    @Test
    void createsReadsUpdatesAndDeletesCompanyWithServices() {
        Company company = new Company("Test Security Sdn Bhd");
        company.setCity("Kuala Lumpur");
        company.setState("Kuala Lumpur");
        company.addService("SOC", "https://example.com/services");

        Company saved = repository.saveAndFlush(company);
        UUID id = saved.getId();

        Company found = repository.findById(id).orElseThrow();
        assertThat(found.getName()).isEqualTo("Test Security Sdn Bhd");
        assertThat(found.getServices()).extracting(CompanyServiceEntity::getService).containsExactly("SOC");

        found.markContacted(true);
        repository.saveAndFlush(found);
        assertThat(repository.findById(id).orElseThrow().getContactedAt()).isNotNull();

        repository.deleteById(id);
        repository.flush();
        assertThat(repository.findById(id)).isEmpty();
    }
}

