package com.reactivo.capacidad.domain.model;

import java.util.List;

public record CapacityPage(
        List<Capacity> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
