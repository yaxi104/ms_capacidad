package com.reactivo.capacidad.domain.utils;

import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static com.reactivo.capacidad.domain.constants.Constants.MAX_TECHNOLOGY;
import static com.reactivo.capacidad.domain.constants.Constants.MIN_TECHNOLOGY;

public final class ValidationHelper {

    private ValidationHelper() {
    }

    public static Mono<CapacityIdTechnologies> validateCapacity(CapacityIdTechnologies capacity) {
        return Mono.just(capacity)
                .map(cap -> {
                    String name = (cap.name() != null && !cap.name().isBlank()) ? cap.name() : null;
                    String description = (cap.description() != null && !cap.description().isBlank()) ? cap.description() : null;
                    List<Long> uniqueIds = cap.idTechnologies() != null
                            ? cap.idTechnologies().stream().distinct().toList()
                            : Collections.emptyList();
                    return new CapacityIdTechnologies(name, description, uniqueIds);
                })
                .filter(cap -> cap.name() != null && cap.description() != null)
                .filter(cap -> cap.idTechnologies().size() >= MIN_TECHNOLOGY
                        && cap.idTechnologies().size() <= MAX_TECHNOLOGY);
    }

}