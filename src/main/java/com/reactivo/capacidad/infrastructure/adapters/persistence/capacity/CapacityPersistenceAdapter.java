package com.reactivo.capacidad.infrastructure.adapters.persistence.capacity;

import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.domain.spi.CapacityPersistencePort;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.mapper.CapacityEntityMapper;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.repository.CapacityRepository;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
public class CapacityPersistenceAdapter implements CapacityPersistencePort {

    private final CapacityRepository capacityRepository;
    private final CapacityEntityMapper capacityEntityMapper;
    private final DatabaseClient databaseClient;

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

    @Override
    public Flux<Capacity> findAllPaged(int page, int size, String sortBy, boolean asc) {
        int offset = page * size;

        String order = "ASC";
        if (!asc) order = "DESC";

        String query = "SELECT * FROM capacity ORDER BY " + sortBy + " " + order + " LIMIT " + size + " OFFSET " + offset;

        return databaseClient.sql(query)
                .map((row, meta) -> new Capacity(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("description", String.class)
                ))
                .all();
    }

    @Override
    public Mono<Long> countAll() {
        return capacityRepository.countAll();
    }

    @Override
    public Flux<Capacity> findByIds(List<Long> ids) {
        return databaseClient.sql("""
                            SELECT id, name, description
                            FROM capacity
                            WHERE id IN (:ids)
                        """)
                .bind("ids", ids)
                .map((row, meta) -> new Capacity(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("description", String.class)
                ))
                .all();
    }

}