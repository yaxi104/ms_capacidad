package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.api.BootcampCapacityServicePort;
import com.reactivo.capacidad.domain.api.CapacityServicePort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.domain.model.CapacityWithTechnologies;
import com.reactivo.capacidad.domain.spi.BootcampCapacityPersistencePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BootcampCapacityUseCase implements BootcampCapacityServicePort {

    private final BootcampCapacityPersistencePort bootcampCapacityPersistencePort;
    private final CapacityServicePort capacityServicePort;

    public BootcampCapacityUseCase(BootcampCapacityPersistencePort bootcampCapacityPersistencePort, CapacityServicePort capacityServicePort) {
        this.bootcampCapacityPersistencePort = bootcampCapacityPersistencePort;
        this.capacityServicePort = capacityServicePort;

    }

    @Override
    public Flux<BootcampCapacity> saveAllBootcampCapacity(Flux<BootcampCapacity> bootcampCapacities) {
        return bootcampCapacities
                .filter(Objects::nonNull)
                .collectList()
                .flatMapMany(list -> {
                    if (list.isEmpty()) {
                        return Flux.error(new BusinessException(TechnicalMessage.INVALID_PARAMETERS));
                    }

                    List<Long> capacityIds = list.stream()
                            .map(BootcampCapacity::idCapacity)
                            .distinct()
                            .toList();

                    return capacityServicePort.validateCapacityIdsExist(capacityIds)
                            .flatMapMany(existingIds -> {
                                List<Long> missingIds = capacityIds.stream()
                                        .filter(id -> !existingIds.contains(id))
                                        .toList();

                                if (!missingIds.isEmpty()) {
                                    return Flux.error(new BusinessException(
                                            TechnicalMessage.INVALID_PARAMETERS
                                    ));
                                }

                                return bootcampCapacityPersistencePort.saveAll(Flux.fromIterable(list));
                            });
                });
    }

    @Override
    public Mono<Map<Long, List<CapacityWithTechnologies>>> findCapacitiesByBootcampIds(List<Long> bootcampIds) {
        return bootcampCapacityPersistencePort.findCapacityIdsGroupedByBootcampId(bootcampIds)
                .flatMap(this::buildCapacitiesByBootcamp);
    }

    @Override
    public Mono<Map<Long, List<CapacityWithTechnologies>>> getBootcampIdGroupedCapacities(int page, int size, boolean asc) {
        return bootcampCapacityPersistencePort.getCapacityIdsGroupedBootcampIdAsMap(page, size, asc)
                .flatMap(this::buildCapacitiesByBootcamp);
    }

    private Mono<Map<Long, List<CapacityWithTechnologies>>> buildCapacitiesByBootcamp(Map<Long, List<Long>> bootcampToCapacityIds) {
        List<Long> allCapacityIds = bootcampToCapacityIds.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        if (allCapacityIds.isEmpty()) {
            return Mono.just(Collections.emptyMap());
        }

        return capacityServicePort.buildCapacityWithTechnologiesItems(allCapacityIds)
                .flatMapMany(Flux::fromIterable)
                .collectMap(CapacityWithTechnologies::id)
                .map(capacityById -> bootcampToCapacityIds.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .map(capacityById::get)
                                        .filter(Objects::nonNull)
                                        .toList(),
                                (u, v) -> v,
                                LinkedHashMap::new
                        ))
                );
    }

}
