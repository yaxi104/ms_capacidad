package com.reactivo.capacidad.infrastructure.adapters.persistence.capacity;

import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.domain.spi.CapacityPersistencePort;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.mapper.CapacityEntityMapper;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.repository.CapacityRepository;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
public class CapacityPersistenceAdapter implements CapacityPersistencePort {

    private final CapacityRepository capacityRepository;
    private final CapacityEntityMapper capacityEntityMapper;

    @Override
    public Mono<Capacity> save(Capacity capacity) {
        return capacityRepository.save(capacityEntityMapper.toEntity(capacity))
                .map(capacityEntityMapper::toModel);
    }

    @Override
    public Flux<Capacity> saveAll(List<Capacity> capacities) {
        return capacityRepository.saveAll(
                        capacities.stream()
                                .map(capacityEntityMapper::toEntity)
                                .toList()
                )
                .map(capacityEntityMapper::toModel);
    }

    @Override
    public Mono<Boolean> existByName(String name) {
        return capacityRepository.existsByName(name);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return capacityRepository.deleteById(id);
    }
}
