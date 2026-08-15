package com.int2.company;

import com.int2.common.PagedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) { this.companyService = companyService; }

    @GetMapping
    public PagedResponse<CompanyResponse> companies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String nacsaLicence,
            @RequestParam(required = false) String ranking,
            @RequestParam(required = false) Boolean contacted,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return companyService.findCompanies(filter(search, state, city, service, nacsaLicence, ranking, contacted), page, size);
    }

    @GetMapping("/map")
    public List<CompanyMapResponse> map(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String nacsaLicence,
            @RequestParam(required = false) String ranking,
            @RequestParam(required = false) Boolean contacted) {
        return companyService.findMapCompanies(filter(search, state, city, service, nacsaLicence, ranking, contacted));
    }

    @GetMapping("/{id}")
    public CompanyResponse company(@PathVariable UUID id) { return companyService.findCompany(id); }

    @PatchMapping("/{id}/contacted")
    public CompanyResponse contacted(@PathVariable UUID id, @Valid @RequestBody ContactedRequest request) {
        return companyService.updateContacted(id, request.contacted());
    }

    private CompanyService.CompanyFilter filter(
            String search, String state, String city, String service, String nacsaLicence, String ranking, Boolean contacted) {
        return new CompanyService.CompanyFilter(search, state, city, service, nacsaLicence, ranking, contacted);
    }
}
