package com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.mapper;

import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.infrastructure.adapters.persistence.capacity.entity.CapacityEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapacityEntityMapper {
    Capacity toModel(CapacityEntity entity);

    CapacityEntity toEntity(Capacity capacity);
}
