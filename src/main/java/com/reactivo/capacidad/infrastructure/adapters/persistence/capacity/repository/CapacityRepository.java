package com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.repository;

import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.entity.CapacityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CapacityRepository extends ReactiveCrudRepository<CapacityEntity, Long> {
    Mono<Boolean> existsByName(String name);

    @Query("SELECT COUNT(*) FROM CAPACITY")
    Mono<Long> countAll();
}
