package com.reactivo.capacidad.domain.api;

import com.reactivo.capacidad.domain.model.BootcampCapacity;
import reactor.core.publisher.Flux;

public interface BootcampCapacityServicePort {
    Flux<BootcampCapacity> saveAllBootcampCapacity(Flux<BootcampCapacity> bootcampCapacities);

//    Flux<TechnologySummary> findAllIdTechnologyByIdCapacity(Long idCapacity);
//
//    Mono<Map<Long, List<TechnologySummary>>> findTechnologiesByCapacityIds(List<Long> capacityIds);
//
//    Mono<Map<Long, List<TechnologySummary>>> getCapacidtyIdGroupedTechnologies(int page, int size, boolean asc);
}
