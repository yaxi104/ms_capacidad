package com.reactivo.capacidad.infrastructure.adapters.client;

import com.reactivo.capacidad.domain.model.CapacityTechnology;
import com.reactivo.capacidad.domain.spi.CapacityTechnologyClientPort;
import com.reactivo.capacidad.infrastructure.adapters.client.dto.CapacityTechnologyDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapacityTechnologyClientAdapter implements CapacityTechnologyClientPort {

    private final WebClient capacityTechnologyWebClient;

    @Override
    @CircuitBreaker(name = "capacityTechnology", fallbackMethod = "fallback")
    @Retry(name = "capacityTechnologyRetry")
    @Bulkhead(name = "capacityTechnologyBulkhead")
    public Mono<Void> saveAll(Flux<CapacityTechnology> technologies) {
        return capacityTechnologyWebClient.post()
                .uri("/capacidad-tecnologia")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(toDTO(technologies), CapacityTechnologyDTO.class)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Capacity technologies saved"))
                .doOnError(e -> log.error("Error saving capacity technologies", e));
    }

    @SuppressWarnings("unused")
    private Mono<Void> fallback(Flux<CapacityTechnology> technologies, Throwable ex) {
        log.warn("Fallback executed for CapacityTechnologyClient due to: {}", ex.toString());
        return Mono.error(ex);
    }

    private Flux<CapacityTechnologyDTO> toDTO(Flux<CapacityTechnology> technologies) {
        return technologies.map(tech -> new CapacityTechnologyDTO(
                tech.idTechnology(),
                tech.idCapacity()
        ));
    }

}
