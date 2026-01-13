package com.reactivo.capacidad.domain.model;

import java.util.List;

public record CapacityWithTechnologies(Long id, String name, String description, List<TechnologySummary> technologies) {
}
