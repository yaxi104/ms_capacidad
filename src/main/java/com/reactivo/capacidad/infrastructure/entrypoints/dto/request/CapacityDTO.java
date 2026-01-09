package com.reactivo.capacidad.infrastructure.entrypoints.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CapacityDTO {
    private String name;
    private String description;
    private List<Long> idTechnologies;
}
