package com.reactivo.capacidad.infrastructure.entrypoints.dto.response;

import java.util.List;

public record CapacityWithTechnologiesDTO(
        Long id,
        String name,
        String description,
        List<CapacityTechnologySummaryDTO> technologies
) {
}
