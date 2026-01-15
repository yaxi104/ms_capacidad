package com.reactivo.capacidad.domain.spi;

import com.reactivo.capacidad.domain.model.Capacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapacityPersistencePort {

    Mono<Capacity> save(Capacity capacity);

    Flux<Capacity> saveAll(List<Capacity> capacities);

    Mono<Boolean> existByName(String name);

    Mono<Void> deleteById(Long id);

    Flux<Capacity> findAllPaged(int page, int size, String sortBy, boolean asc);

    Mono<Long> countAll();

    Flux<Capacity> findByIds(List<Long> ids);


    Mono<Void> deleteBootcampCapacity(Long bootcampId);

    Flux<Long> findOrphanedCapacities();

    Mono<Void> deleteCapacity(Long capacityId);

}
