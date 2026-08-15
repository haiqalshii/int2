package com.int2.email;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EmailGenerationRequest(@NotNull UUID companyId) {}

