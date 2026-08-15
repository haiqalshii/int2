package com.int2.candidate;

import java.time.Instant;
import java.util.UUID;

public record CandidateResponse(UUID id, String name, String email, boolean cvUploaded, Instant updatedAt) {
    static CandidateResponse from(CandidateProfile profile) {
        return new CandidateResponse(profile.getId(), profile.getName(), profile.getEmail(),
                profile.getCvFilePath() != null, profile.getUpdatedAt());
    }
}

