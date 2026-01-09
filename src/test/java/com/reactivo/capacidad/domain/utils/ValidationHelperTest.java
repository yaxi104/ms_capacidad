package com.reactivo.capacidad.domain.utils;

import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static com.reactivo.capacidad.domain.constants.Constants.MAX_TECHNOLOGY;
import static com.reactivo.capacidad.domain.constants.Constants.MIN_TECHNOLOGY;

class ValidationHelperTest {

    @Test
    void validateCapacitySuccessTest() {
        CapacityIdTechnologies input = new CapacityIdTechnologies(
                "Cap1",
                "Description",
                List.of(1L, 2L, 3L)
        );

        StepVerifier.create(ValidationHelper.validateCapacity(input))
                .expectNextMatches(cap -> cap.name().equals("Cap1")
                        && cap.description().equals("Description")
                        && cap.idTechnologies().size() == 3)
                .verifyComplete();
    }

    @Test
    void validateCapacityNameOrDescriptionNullTest() {
        CapacityIdTechnologies input1 = new CapacityIdTechnologies(null, "Desc", List.of(1L, 2L, 3L));
        CapacityIdTechnologies input2 = new CapacityIdTechnologies("Cap", null, List.of(1L, 2L, 3L));
        CapacityIdTechnologies input3 = new CapacityIdTechnologies("  ", "Desc", List.of(1L, 2L, 3L));

        StepVerifier.create(ValidationHelper.validateCapacity(input1))
                .expectComplete()
                .verify();

        StepVerifier.create(ValidationHelper.validateCapacity(input2))
                .expectComplete()
                .verify();

        StepVerifier.create(ValidationHelper.validateCapacity(input3))
                .expectComplete()
                .verify();
    }

    @Test
    void validateCapacityTooFewTechnologiesTest() {
        CapacityIdTechnologies input = new CapacityIdTechnologies(
                "Cap",
                "Desc",
                List.of(1L, 2L)
        );

        StepVerifier.create(ValidationHelper.validateCapacity(input))
                .expectComplete()
                .verify();
    }

    @Test
    void validateCapacityTooManyTechnologiesTest() {
        List<Long> techIds = java.util.stream.LongStream.rangeClosed(1, 21).boxed().toList();
        CapacityIdTechnologies input = new CapacityIdTechnologies(
                "Cap",
                "Desc",
                techIds
        );

        StepVerifier.create(ValidationHelper.validateCapacity(input))
                .expectComplete()
                .verify();
    }

    @Test
    void validateCapacityDuplicatesRemovedTest() {
        CapacityIdTechnologies input = new CapacityIdTechnologies(
                "Cap",
                "Desc",
                List.of(1L, 1L, 2L, 3L, 3L)
        );

        StepVerifier.create(ValidationHelper.validateCapacity(input))
                .expectNextMatches(cap -> cap.idTechnologies().size() == 3
                        && cap.idTechnologies().containsAll(List.of(1L, 2L, 3L)))
                .verifyComplete();
    }

    @Test
    void validateCapacityEdgeValuesTest() {
        List<Long> minTechs = java.util.stream.LongStream.rangeClosed(1, MIN_TECHNOLOGY).boxed().toList();
        List<Long> maxTechs = java.util.stream.LongStream.rangeClosed(1, MAX_TECHNOLOGY).boxed().toList();

        StepVerifier.create(ValidationHelper.validateCapacity(new CapacityIdTechnologies("Cap", "Desc", minTechs)))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(ValidationHelper.validateCapacity(new CapacityIdTechnologies("Cap", "Desc", maxTechs)))
                .expectNextCount(1)
                .verifyComplete();
    }
}