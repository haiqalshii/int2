package com.int2.company;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {
    CompanyResponse toResponse(Company company, List<String> services) {
        return new CompanyResponse(company.getId(), company.getName(), company.getDescription(), company.getWebsite(),
                company.getEmail(), company.getPhone(), company.getAddress(), company.getCity(), company.getState(),
                company.getLatitude(), company.getLongitude(), company.getNacsaLicence(), company.getNacsaStatus(),
                company.getSourceUrl(), company.getGoogleRating(), company.getGoogleReviewCount(), List.copyOf(services), company.isContacted(), company.getContactedAt(),
                company.getLastVerifiedAt());
    }

    CompanyMapResponse toMapResponse(Company company) {
        return new CompanyMapResponse(company.getId(), company.getName(), company.getLatitude(), company.getLongitude(),
                company.getCity(), company.getState(), company.getGoogleRating(), company.getGoogleReviewCount(), company.isContacted());
    }
}
