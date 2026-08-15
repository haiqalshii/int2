package com.int2.company;

import jakarta.validation.constraints.NotNull;

public record ContactedRequest(@NotNull Boolean contacted) {}

