package com.reactivo.capacidad.infrastructure.adapters.client;

import com.reactivo.capacidad.domain.model.CapacityTechnology;
import com.reactivo.capacidad.domain.model.TechnologySummary;
import com.reactivo.capacidad.domain.spi.CapacityTechnologyClientPort;
import com.reactivo.capacidad.infrastructure.adapters.client.dto.CapacityTechnologyDTO;
import com.reactivo.capacidad.infrastructure.adapters.client.dto.TechnologySummaryResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @CircuitBreaker(name = "capacityTechnology", fallbackMethod = "findTechnologiesFallback")
    @Retry(name = "capacityTechnologyRetry")
    @Bulkhead(name = "capacityTechnologyBulkhead")
    public Mono<Map<Long, List<TechnologySummary>>> findTechnologiesByCapacityIds(List<Long> capacityIds) {
        return capacityTechnologyWebClient.post()
                .uri("/tecnologias/capacidades")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of("capacityIds", capacityIds))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Long, List<TechnologySummaryResponse>>>() {
                })
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .map(r -> new TechnologySummary(r.id(), r.name()))
                                        .collect(Collectors.toList())
                        )));
    }

    @Override
    @CircuitBreaker(name = "capacityTechnology", fallbackMethod = "findTechnologiesPagedFallback")
    @Retry(name = "capacityTechnologyRetry")
    @Bulkhead(name = "capacityTechnologyBulkhead")
    public Mono<Map<Long, List<TechnologySummary>>> findTechnologiesByCapacityIdsPaged(int page, int size, boolean asc) {
        return capacityTechnologyWebClient.post()
                .uri("/tecnologias/capacidades/paginado")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of(
                        "page", page,
                        "size", size,
                        "asc", asc
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Long, List<TechnologySummaryResponse>>>() {})
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .map(r -> new TechnologySummary(r.id(), r.name()))
                                        .collect(Collectors.toList())
                        )));
    }

    @SuppressWarnings("unused")
    private Mono<Void> fallback(Flux<CapacityTechnology> technologies, Throwable ex) {
        log.warn("Fallback executed for CapacityTechnologyClient due to: {}", ex.toString());
        return Mono.error(ex);
    }

    @SuppressWarnings("unused")
    private Mono<Map<Long, List<TechnologySummary>>> findTechnologiesFallback(List<Long> capacityIds, Throwable ex) {
        log.warn("Fallback executed for findTechnologiesByCapacityIds due to: {}", ex.toString());
        return Mono.just(Collections.emptyMap());
    }

    @SuppressWarnings("unused")
    private Mono<Map<Long, List<TechnologySummary>>> findTechnologiesPagedFallback(int page, int size, boolean asc, Throwable ex) {
        log.warn("Fallback executed for findTechnologiesByCapacityIdsPaged due to: {}", ex.toString());
        return Mono.just(Collections.emptyMap());
    }

    private Flux<CapacityTechnologyDTO> toDTO(Flux<CapacityTechnology> technologies) {
        return technologies.map(tech -> new CapacityTechnologyDTO(
                tech.idTechnology(),
                tech.idCapacity()
        ));
    }

}
