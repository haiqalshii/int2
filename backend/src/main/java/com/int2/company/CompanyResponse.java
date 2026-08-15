package com.int2.company;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        String description,
        String website,
        String email,
        String phone,
        String address,
        String city,
        String state,
        BigDecimal latitude,
        BigDecimal longitude,
        String nacsaLicence,
        String nacsaStatus,
        String sourceUrl,
        List<String> services,
        boolean contacted,
        Instant contactedAt,
        Instant lastVerifiedAt) {}

