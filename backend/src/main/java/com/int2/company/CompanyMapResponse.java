package com.int2.company;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyMapResponse(
        UUID id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String city,
        String state,
        BigDecimal googleRating,
        Integer googleReviewCount,
        boolean contacted) {}
