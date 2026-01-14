package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.api.BootcampCapacityServicePort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.domain.spi.BootcampCapacityPersistencePort;
import reactor.core.publisher.Flux;

import java.util.Objects;

public class BootcampCapacityUseCase implements BootcampCapacityServicePort {

    private final BootcampCapacityPersistencePort bootcampCapacityPersistencePort;
//    private final TechnologyPersistencePort technologyPersistencePort;

    public BootcampCapacityUseCase(BootcampCapacityPersistencePort bootcampCapacityPersistencePort) {
        this.bootcampCapacityPersistencePort = bootcampCapacityPersistencePort;

    }

    @Override
    public Flux<BootcampCapacity> saveAllBootcampCapacity(Flux<BootcampCapacity> bootcampCapacities) {
        return bootcampCapacities
                .filter(Objects::nonNull)
                .collectList()
                .flatMapMany(list -> {
                    if (list.isEmpty()) {
                        return Flux.error(
                                new BusinessException(TechnicalMessage.INVALID_PARAMETERS)
                        );
                    }
                    return bootcampCapacityPersistencePort.saveAll(Flux.fromIterable(list));
                });
    }
//
//    @Override
//    public Flux<TechnologySummary> findAllIdTechnologyByIdCapacity(Long idCapacity) {
//        return capacityTechnologyPersistencePort.findAllIdTechnologyByIdCapacity(idCapacity)
//                .collectList()
//                .filter(ids -> !ids.isEmpty())
//                .flatMapMany(technologyPersistencePort::findByIds);
//    }
//
//    @Override
//    public Mono<Map<Long, List<TechnologySummary>>> findTechnologiesByCapacityIds(List<Long> capacityIds) {
//        if (capacityIds == null || capacityIds.isEmpty()) {
//            return Mono.just(Map.of());
//        }
//        return capacityTechnologyPersistencePort.findByIdCapacityIn(capacityIds)
//                .collectList()
//                .flatMap(capacityTechnologies -> {
//                    if (capacityTechnologies.isEmpty()) {
//                        return Mono.just(Map.of());
//                    }
//
//                    Map<Long, List<Long>> capacityToTechIds = capacityTechnologies.stream()
//                            .collect(Collectors.groupingBy(
//                                    CapacityTechnology::idCapacity,
//                                    Collectors.mapping(CapacityTechnology::idTechnology, Collectors.toList())
//                            ));
//
//                    List<Long> allTechnologyIds = capacityToTechIds.values().stream()
//                            .flatMap(List::stream)
//                            .distinct()
//                            .toList();
//
//                    if (allTechnologyIds.isEmpty()) {
//                        return Mono.just(Map.of());
//                    }
//
//                    return technologyPersistencePort.findByIds(allTechnologyIds)
//                            .collectList()
//                            .map(allTechSummaries -> {
//                                Map<Long, List<TechnologySummary>> result = new HashMap<>();
//                                for (Map.Entry<Long, List<Long>> entry : capacityToTechIds.entrySet()) {
//                                    Long capacityId = entry.getKey();
//                                    List<Long> techIds = entry.getValue();
//
//                                    List<TechnologySummary> techSummaries = allTechSummaries.stream()
//                                            .filter(ts -> techIds.contains(ts.id()))
//                                            .toList();
//
//                                    result.put(capacityId, techSummaries);
//                                }
//                                return result;
//                            });
//                });
//    }
//
//    @Override
//    public Mono<Map<Long, List<TechnologySummary>>> getCapacidtyIdGroupedTechnologies(int page, int size, boolean asc) {
//        return capacityTechnologyPersistencePort.getCapacityIdGroupedTechnologiesAsMap(page, size, asc);
//    }

}
