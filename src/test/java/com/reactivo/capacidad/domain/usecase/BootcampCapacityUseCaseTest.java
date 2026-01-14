package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.api.CapacityServicePort;
import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.domain.model.CapacityWithTechnologies;
import com.reactivo.capacidad.domain.spi.BootcampCapacityPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BootcampCapacityUseCaseTest {

    @Mock
    private BootcampCapacityPersistencePort bootcampCapacityPersistencePort;

    @Mock
    private CapacityServicePort capacityServicePort;

    private BootcampCapacityUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new BootcampCapacityUseCase(bootcampCapacityPersistencePort, capacityServicePort);
    }

    @Test
    void saveAllBootcampCapacitySuccessTest() {
        BootcampCapacity cap1 = new BootcampCapacity(null, 1L, 100L);
        BootcampCapacity cap2 = new BootcampCapacity(null, 2L, 101L);

        Flux<BootcampCapacity> input = Flux.just(cap1, cap2);

        when(capacityServicePort.validateCapacityIdsExist(List.of(1L, 2L)))
                .thenReturn(Mono.just(List.of(1L, 2L)));

        when(bootcampCapacityPersistencePort.saveAll(any()))
                .thenReturn(Flux.just(cap1, cap2));

        StepVerifier.create(useCase.saveAllBootcampCapacity(input))
                .expectNext(cap1)
                .expectNext(cap2)
                .verifyComplete();

        verify(bootcampCapacityPersistencePort, times(1)).saveAll(any());
    }

    @Test
    void saveAllBootcampCapacityEmptyFluxThrowsBusinessException() {
        Flux<BootcampCapacity> input = Flux.empty();

        StepVerifier.create(useCase.saveAllBootcampCapacity(input))
                .expectError(BusinessException.class)
                .verify();

        verifyNoInteractions(bootcampCapacityPersistencePort);
        verifyNoInteractions(capacityServicePort);
    }

    @Test
    void saveAllBootcampCapacityWithInvalidIdsThrowsBusinessException() {
        BootcampCapacity cap1 = new BootcampCapacity(null, 1L, 100L);
        BootcampCapacity cap2 = new BootcampCapacity(null, 2L, 101L);

        Flux<BootcampCapacity> input = Flux.just(cap1, cap2);

        when(capacityServicePort.validateCapacityIdsExist(List.of(1L, 2L)))
                .thenReturn(Mono.just(List.of(1L)));

        StepVerifier.create(useCase.saveAllBootcampCapacity(input))
                .expectError(BusinessException.class)
                .verify();

        verify(bootcampCapacityPersistencePort, never()).saveAll(any());
    }

    @Test
    void findCapacitiesByBootcampIdsSuccessTest() {
        List<Long> bootcampIds = List.of(100L, 101L);
        Map<Long, List<Long>> bootcampToCapacityIds = Map.of(
                100L, List.of(1L, 2L),
                101L, List.of(3L)
        );

        when(bootcampCapacityPersistencePort.findCapacityIdsGroupedByBootcampId(bootcampIds))
                .thenReturn(Mono.just(bootcampToCapacityIds));

        List<CapacityWithTechnologies> allCapacities = List.of(
                new CapacityWithTechnologies(1L, "C1", "D1", List.of()),
                new CapacityWithTechnologies(2L, "C2", "D2", List.of()),
                new CapacityWithTechnologies(3L, "C3", "D3", List.of())
        );

        when(capacityServicePort.buildCapacityWithTechnologiesItems(anyList()))
                .thenReturn(Mono.just(allCapacities));

        StepVerifier.create(useCase.findCapacitiesByBootcampIds(bootcampIds))
                .expectNextMatches(map ->
                        map.get(100L).size() == 2 &&
                                map.get(101L).size() == 1
                )
                .verifyComplete();
    }

    @Test
    void getBootcampIdGroupedCapacitiesSuccessTest() {
        int page = 0;
        int size = 10;
        boolean asc = true;

        Map<Long, List<Long>> bootcampToCapacityIds = Map.of(
                100L, List.of(1L, 2L),
                101L, List.of(3L)
        );

        when(bootcampCapacityPersistencePort.getCapacityIdsGroupedBootcampIdAsMap(page, size, asc))
                .thenReturn(Mono.just(bootcampToCapacityIds));

        List<CapacityWithTechnologies> allCapacities = List.of(
                new CapacityWithTechnologies(1L, "C1", "D1", List.of()),
                new CapacityWithTechnologies(2L, "C2", "D2", List.of()),
                new CapacityWithTechnologies(3L, "C3", "D3", List.of())
        );

        when(capacityServicePort.buildCapacityWithTechnologiesItems(anyList()))
                .thenReturn(Mono.just(allCapacities));

        StepVerifier.create(useCase.getBootcampIdGroupedCapacities(page, size, asc))
                .expectNextMatches(map ->
                        map.get(100L).size() == 2 &&
                                map.get(101L).size() == 1
                )
                .verifyComplete();
    }

    @Test
    void getBootcampIdGroupedCapacitiesEmptyTest() {
        when(bootcampCapacityPersistencePort.getCapacityIdsGroupedBootcampIdAsMap(0, 10, true))
                .thenReturn(Mono.just(Map.of()));

        StepVerifier.create(useCase.getBootcampIdGroupedCapacities(0, 10, true))
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();
    }
}
