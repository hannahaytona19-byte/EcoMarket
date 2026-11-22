package io.ecomarket.desktop.schema.dto;

import java.math.BigDecimal;

public record EcoImpactSummary(
        BigDecimal totalCarbonSavedKg,
        int ecoPointsEarned,
        BigDecimal orderTotal
) {}
