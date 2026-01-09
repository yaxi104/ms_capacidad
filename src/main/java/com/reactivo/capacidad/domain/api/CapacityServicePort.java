package com.reactivo.capacidad.domain.api;

import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import reactor.core.publisher.Flux;

public interface CapacityServicePort {
    Flux<Capacity> saveCapacities(Flux<CapacityIdTechnologies> capacities);

}
