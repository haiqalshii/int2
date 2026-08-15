package com.int2.company;

import com.int2.common.PagedResponse;
import com.int2.common.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyServiceRepository companyServiceRepository;
    private final CompanyMapper mapper;

    public CompanyService(CompanyRepository companyRepository, CompanyServiceRepository companyServiceRepository, CompanyMapper mapper) {
        this.companyRepository = companyRepository;
        this.companyServiceRepository = companyServiceRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CompanyResponse> findCompanies(CompanyFilter filter, int page, int size) {
        Page<Company> companies = companyRepository.findAll(specification(filter),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        Map<UUID, List<String>> services = servicesByCompany(companies.getContent());
        return PagedResponse.from(companies.map(company -> mapper.toResponse(
                company, services.getOrDefault(company.getId(), List.of()))));
    }

    @Transactional(readOnly = true)
    public CompanyResponse findCompany(UUID id) {
        Company company = requireCompany(id);
        List<String> services = company.getServices().stream().map(CompanyServiceEntity::getService).sorted().toList();
        return mapper.toResponse(company, services);
    }

    @Transactional(readOnly = true)
    public List<CompanyMapResponse> findMapCompanies(CompanyFilter filter) {
        return companyRepository.findAll(specification(filter), Sort.by("name")).stream()
                .map(mapper::toMapResponse).toList();
    }

    @Transactional
    public CompanyResponse updateContacted(UUID id, boolean contacted) {
        Company company = requireCompany(id);
        company.markContacted(contacted);
        companyRepository.save(company);
        List<String> services = company.getServices().stream().map(CompanyServiceEntity::getService).sorted().toList();
        return mapper.toResponse(company, services);
    }

    private Company requireCompany(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
    }

    private Specification<Company> specification(CompanyFilter filter) {
        return CompanySpecifications.filtered(filter.search(), filter.state(), filter.city(), filter.service(),
                filter.nacsaLicence(), filter.ranking(), filter.contacted());
    }

    private Map<UUID, List<String>> servicesByCompany(List<Company> companies) {
        if (companies.isEmpty()) return Collections.emptyMap();
        List<UUID> ids = companies.stream().map(Company::getId).toList();
        Map<UUID, List<String>> result = new HashMap<>();
        companyServiceRepository.findByCompanyIds(ids).forEach(service -> result
                .computeIfAbsent(service.getCompany().getId(), ignored -> new ArrayList<>())
                .add(service.getService()));
        return result;
    }

    public record CompanyFilter(
            String search, String state, String city, String service, String nacsaLicence, String ranking, Boolean contacted) {}
}
