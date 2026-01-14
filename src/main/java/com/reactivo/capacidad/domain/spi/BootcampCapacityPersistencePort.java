package com.reactivo.capacidad.domain.spi;


import com.reactivo.capacidad.domain.model.BootcampCapacity;
import reactor.core.publisher.Flux;

public interface BootcampCapacityPersistencePort {
    Flux<BootcampCapacity> saveAll(Flux<BootcampCapacity> bootcampCapacity);

//    Flux<Long> findAllIdTechnologyByIdCapacity(Long idCapacity);
//
//    Flux<CapacityTechnology> findByIdCapacityIn(List<Long> idCapacities);
//
//    Mono<Map<Long, List<TechnologySummary>>> getCapacityIdGroupedTechnologiesAsMap(int page, int size, boolean asc);
}
