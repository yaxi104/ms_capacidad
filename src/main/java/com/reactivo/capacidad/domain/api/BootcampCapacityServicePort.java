package com.reactivo.capacidad.domain.api;

import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.domain.model.CapacityWithTechnologies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BootcampCapacityServicePort {
    Flux<BootcampCapacity> saveAllBootcampCapacity(Flux<BootcampCapacity> bootcampCapacities);

    Mono<Map<Long, List<CapacityWithTechnologies>>> findCapacitiesByBootcampIds(List<Long> bootcampIds);

    Mono<Map<Long, List<CapacityWithTechnologies>>> getBootcampIdGroupedCapacities(int page, int size, boolean asc);
}
