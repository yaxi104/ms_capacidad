package com.reactivo.capacidad.domain.spi;

import com.reactivo.capacidad.domain.model.CapacityTechnology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityTechnologyClientPort {

    Mono<Void> saveAll(Flux<CapacityTechnology> technologies);
}
