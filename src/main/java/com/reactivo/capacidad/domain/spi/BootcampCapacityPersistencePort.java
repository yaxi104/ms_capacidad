package com.reactivo.capacidad.domain.spi;


import com.reactivo.capacidad.domain.model.BootcampCapacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BootcampCapacityPersistencePort {
    Flux<BootcampCapacity> saveAll(Flux<BootcampCapacity> bootcampCapacity);

    Flux<BootcampCapacity> findByIdBootcampIn(List<Long> bootcampIds);

    Mono<Map<Long, List<Long>>> findCapacityIdsGroupedByBootcampId(List<Long> bootcampIds);

    Mono<Map<Long, List<Long>>> getCapacityIdsGroupedBootcampIdAsMap(int page, int size, boolean asc);

}
