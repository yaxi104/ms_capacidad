package com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.repository;

import com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.entity.BootcampCapacityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface BootcampCapacityRepository extends ReactiveCrudRepository<BootcampCapacityEntity, Long> {

    Flux<Long> findIdCapacityByIdBootcamp(Long idBootcamp);

    Flux<BootcampCapacityEntity> findByIdBootcampIn(List<Long> idBootcamps);

}
