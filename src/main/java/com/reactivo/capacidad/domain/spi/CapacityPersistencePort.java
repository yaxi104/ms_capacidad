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
}
