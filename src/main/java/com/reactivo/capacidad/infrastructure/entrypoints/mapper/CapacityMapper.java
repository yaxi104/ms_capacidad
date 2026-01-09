package com.reactivo.capacidad.infrastructure.entrypoints.mapper;

import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import com.reactivo.capacidad.infrastructure.entrypoints.dto.request.CapacityDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapacityMapper {

    CapacityIdTechnologies toDomain(CapacityDTO capacityDTO);
}
