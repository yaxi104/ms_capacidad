package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.api.TransactionalPort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import com.reactivo.capacidad.domain.model.TechnologySummary;
import com.reactivo.capacidad.domain.spi.CapacityPersistencePort;
import com.reactivo.capacidad.domain.spi.CapacityTechnologyClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapacityUseCaseTest {

    @Mock
    private CapacityPersistencePort capacityPersistencePort;

    @Mock
    private CapacityTechnologyClientPort capacityTechnologyClientPort;

    @Mock
    private TransactionalPort transactionalPort;

    private CapacityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CapacityUseCase(capacityPersistencePort, capacityTechnologyClientPort, transactionalPort);
    }

    @Test
    void saveCapacitiesSuccessTest() {
        CapacityIdTechnologies input = new CapacityIdTechnologies("Cap1", "Desc", List.of(1L, 2L, 3L));
        Capacity savedCapacity = new Capacity(1L, "Cap1", "Desc");

        when(capacityPersistencePort.existByName("Cap1")).thenReturn(Mono.just(false));
        when(capacityPersistencePort.save(any())).thenReturn(Mono.just(savedCapacity));
        when(capacityTechnologyClientPort.saveAll(any())).thenReturn(Mono.empty());
        when(transactionalPort.transactional(ArgumentMatchers.<Mono<Capacity>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(useCase.saveCapacities(Flux.just(input)))
                .expectNextMatches(cap -> cap.name().equals("Cap1") && cap.id().equals(1L))
                .verifyComplete();

        verify(capacityPersistencePort).existByName("Cap1");
        verify(capacityPersistencePort).save(any());
        verify(capacityTechnologyClientPort).saveAll(any());
    }

    @Test
    void saveCapacitiesAlreadyExistsTest() {
        CapacityIdTechnologies input = new CapacityIdTechnologies("Cap1", "Desc", List.of(1L, 2L, 3L));

        when(capacityPersistencePort.existByName("Cap1")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.saveCapacities(Flux.just(input)))
                .expectComplete() // Ahora completamos sin error
                .verify();

        verify(capacityPersistencePort).existByName("Cap1");
        verify(capacityPersistencePort, never()).save(any());
        verify(capacityTechnologyClientPort, never()).saveAll(any());
    }

    @Test
    void saveCapacitiesValidationFailTest() {
        CapacityIdTechnologies input1 = new CapacityIdTechnologies(null, null, List.of());
        CapacityIdTechnologies input2 = new CapacityIdTechnologies("  ", "  ", List.of());

        StepVerifier.create(useCase.saveCapacities(Flux.just(input1, input2)))
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getTechnicalMessage() == TechnicalMessage.INVALID_REQUEST)
                .verify();

        verifyNoInteractions(capacityPersistencePort);
        verifyNoInteractions(capacityTechnologyClientPort);
    }

    @Test
    void saveCapacitiesTechnologySaveFailRollbackTest() {
        CapacityIdTechnologies input = new CapacityIdTechnologies("Cap1", "Desc", List.of(1L, 2L, 3L));
        Capacity savedCapacity = new Capacity(1L, "Cap1", "Desc");

        when(capacityPersistencePort.existByName("Cap1")).thenReturn(Mono.just(false));
        when(capacityPersistencePort.save(any())).thenReturn(Mono.just(savedCapacity));
        when(capacityTechnologyClientPort.saveAll(any())).thenReturn(Mono.error(new RuntimeException("Tech fail")));
        when(capacityPersistencePort.deleteById(1L)).thenReturn(Mono.empty());
        when(transactionalPort.transactional(ArgumentMatchers.<Mono<Capacity>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(useCase.saveCapacities(Flux.just(input)))
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getTechnicalMessage() == TechnicalMessage.CLIENTE_TECHNOLOGY_FAILED)
                .verify();

        verify(capacityPersistencePort).deleteById(1L);
        verify(capacityTechnologyClientPort).saveAll(any());
    }

    @Test
    void saveCapacitiesEmptyFluxTest() {
        StepVerifier.create(useCase.saveCapacities(Flux.empty()))
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getTechnicalMessage() == TechnicalMessage.INVALID_REQUEST)
                .verify();

        verifyNoInteractions(capacityPersistencePort);
        verifyNoInteractions(capacityTechnologyClientPort);
    }

    @Test
    void saveCapacitiesPartialExistingAndNewTest() {
        CapacityIdTechnologies existing = new CapacityIdTechnologies("CapExist", "DescExist", List.of(1L, 2L, 3L));
        CapacityIdTechnologies newCap = new CapacityIdTechnologies("CapNew", "DescNew", List.of(4L, 5L, 6L));
        Capacity savedNewCapacity = new Capacity(2L, "CapNew", "DescNew");

        when(capacityPersistencePort.existByName("CapExist")).thenReturn(Mono.just(true));
        when(capacityPersistencePort.existByName("CapNew")).thenReturn(Mono.just(false));
        when(capacityPersistencePort.save(any())).thenReturn(Mono.just(savedNewCapacity));
        when(capacityTechnologyClientPort.saveAll(any())).thenReturn(Mono.empty());
        when(transactionalPort.transactional(ArgumentMatchers.<Mono<Capacity>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(useCase.saveCapacities(Flux.just(existing, newCap)))
                .expectNextMatches(cap -> cap.name().equals("CapNew") && cap.id().equals(2L))
                .verifyComplete();

        verify(capacityPersistencePort).existByName("CapExist");
        verify(capacityPersistencePort).existByName("CapNew");
        verify(capacityPersistencePort).save(any());
        verify(capacityTechnologyClientPort).saveAll(any());
    }

    @Test
    void findPagedCapacitiesByNameTest() {
        Capacity cap1 = new Capacity(1L, "A", "Desc A");
        Capacity cap2 = new Capacity(2L, "B", "Desc B");
        List<Capacity> capacities = List.of(cap1, cap2);
        List<Long> capacityIds = capacities.stream().map(Capacity::id).toList();
        Map<Long, List<TechnologySummary>> techMap = Map.of(
                1L, List.of(new TechnologySummary(10L, "Tech1")),
                2L, List.of(new TechnologySummary(20L, "Tech2"))
        );

        when(capacityPersistencePort.findAllPaged(0, 2, "name", true))
                .thenReturn(Flux.fromIterable(capacities));
        when(capacityPersistencePort.countAll()).thenReturn(Mono.just(2L));
        when(capacityTechnologyClientPort.findTechnologiesByCapacityIds(capacityIds))
                .thenReturn(Mono.just(techMap));

        StepVerifier.create(useCase.findPagedCapacities(0, 2, "name", true))
                .assertNext(response -> {
                    assertEquals(2, response.getItems().size());
                    assertEquals("A", response.getItems().get(0).name());
                    assertEquals(1L, response.getItems().get(0).id());
                    assertEquals("Tech1", response.getItems().get(0).technologies().get(0).name());
                })
                .verifyComplete();
    }

    @Test
    void findPagedCapacitiesByTechnologyCountEmptyTest() {
        when(capacityTechnologyClientPort.getCapacityIdGroupedTechnologies(0, 2, true))
                .thenReturn(Mono.just(Collections.emptyMap()));
        when(capacityPersistencePort.countAll()).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.findPagedCapacities(0, 2, "technologyCount", true))
                .assertNext(response -> {
                    assertTrue(response.getItems().isEmpty());
                    assertEquals(0, response.getTotalItems());
                })
                .verifyComplete();
    }

    @Test
    void findPagedCapacitiesByTechnologyCountWithDataTest() {
        Capacity cap1 = new Capacity(1L, "Cap1", "Desc1");
        Capacity cap2 = new Capacity(2L, "Cap2", "Desc2");

        Map<Long, List<TechnologySummary>> techMap = new LinkedHashMap<>();
        techMap.put(2L, List.of(new TechnologySummary(20L, "TechB")));
        techMap.put(1L, List.of(new TechnologySummary(10L, "TechA")));

        when(capacityTechnologyClientPort.getCapacityIdGroupedTechnologies(0, 2, true))
                .thenReturn(Mono.just(techMap));
        when(capacityPersistencePort.findByIds(List.of(2L, 1L)))
                .thenReturn(Flux.just(cap2, cap1));
        when(capacityPersistencePort.countAll()).thenReturn(Mono.just(2L));

        StepVerifier.create(useCase.findPagedCapacities(0, 2, "technologyCount", true))
                .assertNext(response -> {
                    assertEquals(2, response.getItems().size());
                    assertEquals(2L, response.getItems().get(0).id());
                    assertEquals("Cap2", response.getItems().get(0).name());
                    assertEquals("TechB", response.getItems().get(0).technologies().get(0).name());

                    assertEquals(1L, response.getItems().get(1).id());
                    assertEquals("Cap1", response.getItems().get(1).name());
                    assertEquals("TechA", response.getItems().get(1).technologies().get(0).name());
                })
                .verifyComplete();
    }

    @Test
    void findPagedCapacitiesByTechnologyCountPartialMissingCapTest() {
        Capacity cap1 = new Capacity(1L, "Cap1", "Desc1");

        Map<Long, List<TechnologySummary>> techMap = new LinkedHashMap<>();
        techMap.put(2L, List.of(new TechnologySummary(20L, "TechB")));
        techMap.put(1L, List.of(new TechnologySummary(10L, "TechA")));

        when(capacityTechnologyClientPort.getCapacityIdGroupedTechnologies(0, 2, true))
                .thenReturn(Mono.just(techMap));
        when(capacityPersistencePort.findByIds(List.of(2L, 1L)))
                .thenReturn(Flux.just(cap1));
        when(capacityPersistencePort.countAll()).thenReturn(Mono.just(1L));

        StepVerifier.create(useCase.findPagedCapacities(0, 2, "technologyCount", true))
                .assertNext(response -> {
                    assertEquals(1, response.getItems().size());
                    assertEquals(1L, response.getItems().get(0).id());
                    assertEquals("Cap1", response.getItems().get(0).name());
                })
                .verifyComplete();
    }

}