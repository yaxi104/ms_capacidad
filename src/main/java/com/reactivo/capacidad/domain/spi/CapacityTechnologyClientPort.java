package com.reactivo.capacidad.domain.spi;

import com.reactivo.capacidad.domain.model.CapacityTechnology;
import com.reactivo.capacidad.domain.model.TechnologySummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CapacityTechnologyClientPort {

    Mono<Void> saveAll(Flux<CapacityTechnology> technologies);

    Mono<Map<Long, List<TechnologySummary>>> findTechnologiesByCapacityIds(List<Long> capacityIds);

}
