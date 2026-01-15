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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

        if ("count".equalsIgnoreCase(sortBy)) {
            return capacityTechnologyClientPort
                    .getCapacityIdGroupedTechnologies(page, size, asc)
                    .flatMap(techMap -> {
                        if (techMap.isEmpty()) {
                            return totalItemsMono.map(total ->
                                    new PagedCapacityResponse(List.of(), page, size, total)
                            );
                        }

                        List<Long> orderedCapacityIds = new ArrayList<>(techMap.keySet());

                        return capacityPersistencePort.findByIds(orderedCapacityIds)
                                .collectList()
                                .zipWith(totalItemsMono)
                                .map(tuple -> {
                                    List<Capacity> capacities = tuple.getT1();
                                    long totalItems = tuple.getT2();

                                    Map<Long, Capacity> capacityById = capacities.stream()
                                            .collect(Collectors.toMap(Capacity::id, c -> c));

                                    List<CapacityWithTechnologies> items = orderedCapacityIds.stream()
                                            .map(id -> {
                                                Capacity cap = capacityById.get(id);
                                                if (cap == null) return null;

                                                return new CapacityWithTechnologies(
                                                        cap.id(),
                                                        cap.name(),
                                                        cap.description(),
                                                        techMap.getOrDefault(id, List.of())
                                                );
                                            })
                                            .filter(Objects::nonNull)
                                            .toList();

                                    return new PagedCapacityResponse(items, page, size, totalItems);
                                });
                    });
        }
        Flux<Capacity> capacitiesFlux = capacityPersistencePort.findAllPaged(page, size, "name", asc);

        return Mono.zip(capacitiesFlux.collectList(), totalItemsMono)
                .flatMap(tuple -> {
                    List<Capacity> capacities = tuple.getT1();
                    long totalItems = tuple.getT2();

                    List<Long> capacityIds = capacities.stream()
                            .map(Capacity::id)
                            .toList();

                    return capacityTechnologyClientPort.findTechnologiesByCapacityIds(capacityIds)
                            .map(techMap -> buildResponsePagedCapacityResponse(capacities, capacityIds, techMap, page, size, totalItems));
                });
    }

    @Override
    public Mono<List<CapacityWithTechnologies>> buildCapacityWithTechnologiesItems(List<Long> capacityIds) {
        Mono<List<Capacity>> capacitiesMono = capacityPersistencePort.findByIds(capacityIds).collectList();

        Mono<Map<Long, List<TechnologySummary>>> technologiesMono = capacityTechnologyClientPort.findTechnologiesByCapacityIds(capacityIds);

        return Mono.zip(capacitiesMono, technologiesMono)
                .map(tuple ->
                        mapToCapacityWithTechnologies(
                                tuple.getT1(),
                                capacityIds,
                                tuple.getT2()
                        )
                );
    }

    @Override
    public Mono<List<Long>> validateCapacityIdsExist(List<Long> capacityIds) {
        if (capacityIds.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        return capacityPersistencePort.findByIds(capacityIds)
                .map(Capacity::id)
                .collectList();
    }

    @Override
    public Mono<List<Long>> deleteCapacitiesByBootcamp(Long bootcampId) {
        return capacityPersistencePort.deleteBootcampCapacity(bootcampId)
                .thenMany(capacityPersistencePort.findOrphanedCapacities())
                .collectList()
                .flatMap(ids -> Flux.fromIterable(ids)
                        .flatMap(capacityPersistencePort::deleteCapacity)
                        .then(Mono.just(ids))
                );
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
            List<Long> orderedCapacityIds,
            Map<Long, List<TechnologySummary>> technologiesByCapacity,
            int page,
            int size,
            long totalItems
    ) {
        List<CapacityWithTechnologies> items =
                mapToCapacityWithTechnologies(capacities, orderedCapacityIds, technologiesByCapacity);

        return new PagedCapacityResponse(items, page, size, totalItems);
    }

    private List<CapacityWithTechnologies> mapToCapacityWithTechnologies(List<Capacity> capacities,
                                                                         List<Long> orderedCapacityIds,
                                                                         Map<Long, List<TechnologySummary>> technologiesByCapacity
    ) {
        List<CapacityWithTechnologies> items = new ArrayList<>();

        for (Long capacityId : orderedCapacityIds) {
            Capacity cap = capacities.stream()
                    .filter(c -> c.id().equals(capacityId))
                    .findFirst()
                    .orElseThrow();

            List<TechnologySummary> techs = technologiesByCapacity
                    .getOrDefault(capacityId, Collections.emptyList())
                    .stream()
                    .sorted(Comparator.comparing(TechnologySummary::id))
                    .toList();

            items.add(new CapacityWithTechnologies(
                    cap.id(),
                    cap.name(),
                    cap.description(),
                    techs
            ));
        }

        return items;
    }

}