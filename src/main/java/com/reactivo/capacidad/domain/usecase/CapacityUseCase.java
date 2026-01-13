package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.api.CapacityServicePort;
import com.reactivo.capacidad.domain.api.TransactionalPort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import com.reactivo.capacidad.domain.model.CapacityTechnology;
import com.reactivo.capacidad.domain.model.CapacityWithTechnologies;
import com.reactivo.capacidad.domain.model.PagedCapacityResponse;
import com.reactivo.capacidad.domain.model.TechnologySummary;
import com.reactivo.capacidad.domain.spi.CapacityPersistencePort;
import com.reactivo.capacidad.domain.spi.CapacityTechnologyClientPort;
import com.reactivo.capacidad.domain.utils.ValidationHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CapacityUseCase implements CapacityServicePort {

    private final CapacityPersistencePort capacityPersistencePort;
    private final CapacityTechnologyClientPort capacityTechnologyClientPort;
    private final TransactionalPort transactionalPort;

    public CapacityUseCase(CapacityPersistencePort capacityPersistencePort,
                           CapacityTechnologyClientPort capacityTechnologyClientPort,
                           TransactionalPort transactionalPort) {
        this.capacityPersistencePort = capacityPersistencePort;
        this.capacityTechnologyClientPort = capacityTechnologyClientPort;
        this.transactionalPort = transactionalPort;
    }

    public Flux<Capacity> saveCapacities(Flux<CapacityIdTechnologies> capacities) {
        return capacities
                .flatMap(ValidationHelper::validateCapacity)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.INVALID_REQUEST)))
                .filterWhen(cap ->
                        capacityPersistencePort.existByName(cap.name())
                                .map(exists -> !exists)
                )
                .flatMap(cap -> transactionalPort.transactional(
                        capacityPersistencePort.save(createCapacity(cap))
                                .flatMap(saved -> saveTechnologies(saved, cap.idTechnologies())
                                        .thenReturn(saved)
                                        .onErrorResume(e -> rollback(saved)
                                                .then(Mono.error(new BusinessException(TechnicalMessage.CLIENTE_TECHNOLOGY_FAILED))))
                                )
                ));
    }

    @Override
    public Mono<PagedCapacityResponse> findPagedCapacities(int page, int size, String sortBy, boolean asc) {
        Mono<Long> totalItemsMono = capacityPersistencePort.countAll();

        if ("technologyCount".equalsIgnoreCase(sortBy)) {
            return capacityTechnologyClientPort.findTechnologiesByCapacityIdsPaged(page, size, asc)
                    .flatMap(techMap -> {
                        List<Long> capacityIds = new ArrayList<>(techMap.keySet());
                        return capacityPersistencePort.findByIds(capacityIds).collectList()
                                .zipWith(totalItemsMono)
                                .map(tuple -> buildResponsePagedCapacityResponse(
                                        tuple.getT1(),
                                        techMap,
                                        page,
                                        size,
                                        tuple.getT2()
                                ));
                    });
        } else {
            Flux<Capacity> capacitiesFlux = capacityPersistencePort.findAllPaged(page, size, "name", asc);
            return Mono.zip(capacitiesFlux.collectList(), totalItemsMono)
                    .flatMap(tuple -> {
                        List<Capacity> capacities = tuple.getT1();
                        long totalItems = tuple.getT2();
                        List<Long> capacityIds = capacities.stream()
                                .map(Capacity::id)
                                .toList();
                        return capacityTechnologyClientPort.findTechnologiesByCapacityIds(capacityIds)
                                .map(map -> buildResponsePagedCapacityResponse(capacities, map, page, size, totalItems));
                    });
        }
    }

    private Mono<Void> saveTechnologies(Capacity capacity, List<Long> techIds) {
        return capacityTechnologyClientPort.saveAll(
                Flux.fromIterable(techIds)
                        .map(idTech -> new CapacityTechnology(idTech, capacity.id()))
        );
    }

    private Mono<Void> rollback(Capacity capacity) {
        return capacityPersistencePort.deleteById(capacity.id());
    }

    private Capacity createCapacity(CapacityIdTechnologies cap) {
        return new Capacity(null, cap.name(), cap.description());
    }

    private PagedCapacityResponse buildResponsePagedCapacityResponse(
            List<Capacity> capacities,
            Map<Long, List<TechnologySummary>> technologiesByCapacity,
            int page,
            int size,
            long totalItems
    ) {
        List<CapacityWithTechnologies> items = capacities.stream()
                .map(cap -> new CapacityWithTechnologies(
                        cap.id(),
                        cap.name(),
                        cap.description(),
                        technologiesByCapacity.getOrDefault(cap.id(), Collections.emptyList())
                ))
                .toList();

        return new PagedCapacityResponse(items, page, size, totalItems);
    }

}
