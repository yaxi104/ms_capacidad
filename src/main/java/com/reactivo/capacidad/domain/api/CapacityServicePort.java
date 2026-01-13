package com.reactivo.capacidad.domain.api;

import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import com.reactivo.capacidad.domain.model.PagedCapacityResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapacityServicePort {
    Flux<Capacity> saveCapacities(Flux<CapacityIdTechnologies> capacities);

    Mono<PagedCapacityResponse> findPagedCapacities(int page, int size, String sortBy, boolean asc);
}
