package com.reactivo.capacidad.infrastructure.entrypoints.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BootcampIdsRequest {
    private List<Long> bootcampIds;
}
