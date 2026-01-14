package com.reactivo.capacidad.infrastructure.adapters.persistence.bootcampcapacity.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "BOOTCAMP_CAPACITY")
public class BootcampCapacityEntity {
    @Id
    private Long id;
    @JsonProperty("id_capacity")
    private Long idCapacity;
    @JsonProperty("id_bootcamp")
    private Long idBootcamp;

    public BootcampCapacityEntity() {
    }

    public BootcampCapacityEntity(Long id, Long idCapacity, Long idBootcamp) {
        this.id = id;
        this.idCapacity = idCapacity;
        this.idBootcamp = idBootcamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdCapacity() {
        return idCapacity;
    }

    public void setIdCapacity(Long idCapacity) {
        this.idCapacity = idCapacity;
    }

    public Long getIdBootcamp() {
        return idBootcamp;
    }

    public void setIdBootcamp(Long idBootcamp) {
        this.idBootcamp = idBootcamp;
    }
}
