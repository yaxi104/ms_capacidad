package com.reactivo.capacidad.domain.usecase;

import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.domain.spi.BootcampCapacityPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class BootcampCapacityUseCaseTest {

    @Mock
    private BootcampCapacityPersistencePort bootcampCapacityPersistencePort;

    private BootcampCapacityUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new BootcampCapacityUseCase(bootcampCapacityPersistencePort);
    }

    @Test
    void saveAllBootcampCapacitySuccessTest() {
        BootcampCapacity cap1 = new BootcampCapacity(null, 1L, 100L);
        BootcampCapacity cap2 = new BootcampCapacity(null, 2L, 101L);

        Flux<BootcampCapacity> input = Flux.just(cap1, cap2);

        when(bootcampCapacityPersistencePort.saveAll(any()))
                .thenReturn(Flux.just(cap1, cap2));

        StepVerifier.create(useCase.saveAllBootcampCapacity(input))
                .expectNext(cap1)
                .expectNext(cap2)
                .verifyComplete();

        verify(bootcampCapacityPersistencePort, times(1)).saveAll(any());
    }

    @Test
    void saveAllBootcampCapacityEptyFluxBusinessExceptionTest() {
        Flux<BootcampCapacity> input = Flux.empty();

        StepVerifier.create(useCase.saveAllBootcampCapacity(input))
                .expectError(BusinessException.class)
                .verify();

        verifyNoInteractions(bootcampCapacityPersistencePort);
    }

}
