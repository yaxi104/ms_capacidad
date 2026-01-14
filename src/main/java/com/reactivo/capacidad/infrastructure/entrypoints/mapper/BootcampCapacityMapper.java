package com.reactivo.capacidad.infrastructure.entrypoints.mapper;

import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.infrastructure.entrypoints.dto.request.BootcampCapacityDTO;
import org.springframework.stereotype.Component;

@Component
public class BootcampCapacityMapper {

    public BootcampCapacity toModel(BootcampCapacityDTO dto) {
        if (dto == null) return null;

        return new BootcampCapacity(
                null,
                dto.idCapacity(),
                dto.idBootcamp()
        );
    }
}
