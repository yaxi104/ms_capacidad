package com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.mapper;

import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.entity.BootcampCapacityEntity;
import org.mapstruct.Mapper;

import java.util.Objects;

@Mapper(componentModel = "spring")
public interface BootcampCapacityEntityMapper {

    default BootcampCapacity toModel(BootcampCapacityEntity entity) {
        Objects.requireNonNull(entity, "BootcampCapacityEntity no puede ser null");
        return new BootcampCapacity(
                entity.getId(),
                entity.getIdCapacity(),
                entity.getIdBootcamp()
        );
    }

    default BootcampCapacityEntity toEntity(BootcampCapacity bootcampCapacity) {
        Objects.requireNonNull(bootcampCapacity, "BootcampCapacity no puede ser null");
        return new BootcampCapacityEntity(
                bootcampCapacity.id(),
                bootcampCapacity.idCapacity(),
                bootcampCapacity.idBootcamp()
        );
    }
}
