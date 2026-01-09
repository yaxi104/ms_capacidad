package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.api.TransactionalPort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.model.Capacity;
import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
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

import java.util.List;

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
        // Mezcla de uno existente y otro nuevo
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
}