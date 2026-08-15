package com.int2.company;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

final class CompanySpecifications {
    private CompanySpecifications() {}

    static Specification<Company> filtered(
            String search, String state, String city, String service, String nacsaLicence, String ranking, Boolean contacted) {
        Specification<Company> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (StringUtils.hasText(search)) {
            String term = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), term),
                    cb.like(cb.lower(root.get("city")), term),
                    cb.like(cb.lower(root.get("state")), term)));
        }
        if (StringUtils.hasText(state)) specification = specification.and(equalsIgnoreCase("state", state));
        if (StringUtils.hasText(city)) specification = specification.and(equalsIgnoreCase("city", city));
        if (StringUtils.hasText(nacsaLicence)) {
            String term = "%" + nacsaLicence.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.like(cb.lower(root.get("nacsaLicence")), term));
        }
        if ("top".equalsIgnoreCase(ranking)) specification = specification.and((root, query, cb) -> cb.and(cb.greaterThanOrEqualTo(root.get("googleRating"), 4.5), cb.greaterThanOrEqualTo(root.get("googleReviewCount"), 20)));
        if ("high".equalsIgnoreCase(ranking)) specification = specification.and((root, query, cb) -> cb.and(cb.greaterThanOrEqualTo(root.get("googleRating"), 4.0), cb.greaterThanOrEqualTo(root.get("googleReviewCount"), 10)));
        if ("popular".equalsIgnoreCase(ranking)) specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("googleReviewCount"), 100));
        if (contacted != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("contacted"), contacted));
        }
        if (StringUtils.hasText(service)) {
            specification = specification.and((root, query, cb) -> {
                query.distinct(true);
                return cb.equal(cb.lower(root.join("services", JoinType.INNER).get("service")), service.trim().toLowerCase());
            });
        }
        return specification;
    }

    private static Specification<Company> equalsIgnoreCase(String field, String value) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase());
    }
}
