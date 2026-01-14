package com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity;

import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.domain.spi.BootcampCapacityPersistencePort;
import com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.mapper.BootcampCapacityEntityMapper;
import com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.repository.BootcampCapacityRepository;
import com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.util.DatabaseConstants;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BootcampCapacityPersistenceAdapter implements BootcampCapacityPersistencePort {

    private final BootcampCapacityRepository bootcampCapacityRepository;
    private final BootcampCapacityEntityMapper bootcampCapacityEntityMapper;
    private final TransactionalOperator transactionalOperator;
    private final DatabaseClient databaseClient;


    @Override
    public Flux<BootcampCapacity> saveAll(Flux<BootcampCapacity> capacities) {
        return transactionalOperator.execute(status ->
                capacities
                        .map(bootcampCapacityEntityMapper::toEntity)
                        .as(bootcampCapacityRepository::saveAll)
        ).map(bootcampCapacityEntityMapper::toModel);
    }

    @Override
    public Flux<BootcampCapacity> findByIdBootcampIn(List<Long> bootcampIds) {
        if (bootcampIds.isEmpty()) {
            return Flux.empty();
        }

        return databaseClient.sql("""
                        SELECT %s, %s
                        FROM CAPACITY.BOOTCAMP_CAPACITY
                        WHERE %s IN (:ids)
                        """.formatted(
                        DatabaseConstants.COLUMN_ID_CAPACITY,
                        DatabaseConstants.COLUMN_ID_BOOTCAMP,
                        DatabaseConstants.COLUMN_ID_BOOTCAMP
                ))
                .bind("ids", bootcampIds)
                .map((row, meta) -> new BootcampCapacity(
                        null,
                        row.get(DatabaseConstants.COLUMN_ID_CAPACITY, Long.class),
                        row.get(DatabaseConstants.COLUMN_ID_BOOTCAMP, Long.class)
                ))
                .all();
    }

    @Override
    public Mono<Map<Long, List<Long>>> findCapacityIdsGroupedByBootcampId(List<Long> bootcampIds) {
        return groupAndSort(
                findByIdBootcampIn(bootcampIds)
                        .map(bc -> Map.entry(
                                bc.idBootcamp(),
                                bc.idCapacity()
                        ))
        );
    }

    @Override
    public Mono<Map<Long, List<Long>>> getCapacityIdsGroupedBootcampIdAsMap(int page, int size, boolean asc) {
        String order = asc ? "ASC" : "DESC";

        Mono<List<Long>> pagedBootcampIds =
                databaseClient.sql(
                                DatabaseConstants.PAGED_BOOTCAMP_IDS_QUERY.formatted(order)
                        )
                        .bind("limit", size)
                        .bind("offset", page * size)
                        .map(row -> row.get(DatabaseConstants.COLUMN_ID_BOOTCAMP, Long.class))
                        .all()
                        .collectList();

        return pagedBootcampIds.flatMap(this::fetchCapacitiesForBootcampIds);
    }

    private Mono<Map<Long, List<Long>>> fetchCapacitiesForBootcampIds(List<Long> bootcampIds) {
        if (bootcampIds.isEmpty()) {
            return Mono.just(Collections.emptyMap());
        }

        return databaseClient.sql(
                        DatabaseConstants.FETCH_CAPACITIES_BY_BOOTCAMP_IDS_QUERY.formatted(
                                DatabaseConstants.COLUMN_ID_BOOTCAMP,
                                DatabaseConstants.COLUMN_ID_CAPACITY,
                                DatabaseConstants.COLUMN_ID_BOOTCAMP,
                                DatabaseConstants.COLUMN_ID_BOOTCAMP,
                                DatabaseConstants.COLUMN_ID_CAPACITY
                        )
                )
                .bind("bootcampIds", bootcampIds)
                .map((row, meta) -> Map.entry(
                        Objects.requireNonNull(row.get(DatabaseConstants.COLUMN_ID_BOOTCAMP, Long.class)),
                        Objects.requireNonNull(row.get(DatabaseConstants.COLUMN_ID_CAPACITY, Long.class))
                ))
                .all()
                .collectMultimap(Map.Entry::getKey, Map.Entry::getValue)
                .map(multimap -> {
                    Map<Long, List<Long>> orderedResult = new LinkedHashMap<>();

                    for (Long bootcampId : bootcampIds) {
                        List<Long> capacities = multimap.getOrDefault(bootcampId, List.of())
                                .stream()
                                .sorted()
                                .toList();

                        orderedResult.put(bootcampId, capacities);
                    }

                    return orderedResult;
                });
    }

    private Mono<Map<Long, List<Long>>> groupAndSort(Flux<Map.Entry<Long, Long>> source) {
        return source
                .collectMultimap(Map.Entry::getKey, Map.Entry::getValue)
                .map(multimap -> multimap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .sorted()
                                        .toList()
                        ))
                );
    }
}
