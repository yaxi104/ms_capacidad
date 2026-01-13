package com.reactivo.capacidad.infrastructure.entrypoints.handler;

import com.reactivo.capacidad.domain.api.CapacityServicePort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.exceptions.BusinessException;
import com.reactivo.capacidad.domain.exceptions.TechnicalException;
import com.reactivo.capacidad.domain.model.CapacityIdTechnologies;
import com.reactivo.capacidad.infrastructure.entrypoints.dto.request.CapacityDTO;
import com.reactivo.capacidad.infrastructure.entrypoints.mapper.CapacityMapper;
import com.reactivo.capacidad.infrastructure.entrypoints.util.ErrorDTO;
import com.reactivo.capacidad.infrastructure.entrypoints.util.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.reactivo.capacidad.infrastructure.entrypoints.util.Constants.AUTH_TOKEN;
import static com.reactivo.capacidad.infrastructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapacityHandlerImpl {

    private final CapacityServicePort capacityServicePort;
    private final CapacityMapper capacityMapper;
    private final HandlerUtils handlerUtils;

    public Mono<ServerResponse> createCapacity(ServerRequest request) {
        String messageId = handlerUtils.getMessageId(request);
        String token = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        Flux<CapacityIdTechnologies> capacityFlux = request.bodyToFlux(CapacityDTO.class)
                .map(capacityMapper::toDomain)
                .filter(Objects::nonNull);

        return capacityServicePort.saveCapacities(capacityFlux)
                .collectList()
                .flatMap(savedCapacities -> {
                    if (savedCapacities.isEmpty()) {
                        return handlerUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                messageId,
                                TechnicalMessage.INVALID_PARAMETERS,
                                List.of(ErrorDTO.builder()
                                        .code(TechnicalMessage.INVALID_PARAMETERS.getCode())
                                        .message("No capacities were saved")
                                        .build()));
                    }
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(savedCapacities);
                })
                .contextWrite(ctx -> {
                    if (messageId != null) ctx = ctx.put(X_MESSAGE_ID, messageId);
                    if (token != null) ctx = ctx.put(AUTH_TOKEN, token);
                    return ctx;
                })
                .doOnSuccess(v -> log.info("Capacities created successfully. messageId={}", messageId))
                .doOnError(ex -> log.error("Error creating capacities. messageId={}", messageId, ex))
                .onErrorResume(BusinessException.class, ex ->
                        handlerUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                messageId,
                                ex.getTechnicalMessage(),
                                List.of(ErrorDTO.builder()
                                        .code(ex.getTechnicalMessage().getCode())
                                        .message(ex.getTechnicalMessage().getMessage())
                                        .param(ex.getTechnicalMessage().getParam())
                                        .build())))
                .onErrorResume(TechnicalException.class, ex ->
                        handlerUtils.buildErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                messageId,
                                ex.getTechnicalMessage(),
                                List.of(ErrorDTO.builder()
                                        .code(ex.getTechnicalMessage().getCode())
                                        .message(ex.getTechnicalMessage().getMessage())
                                        .param(ex.getTechnicalMessage().getParam())
                                        .build())))
                .onErrorResume(ex ->
                        handlerUtils.buildErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                messageId,
                                TechnicalMessage.INTERNAL_ERROR,
                                List.of(ErrorDTO.builder()
                                        .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                        .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                        .build())));
    }

    public Mono<ServerResponse> getPagedCapacities(ServerRequest request) {
        String messageId = handlerUtils.getMessageId(request);
        String token = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        boolean asc = Boolean.parseBoolean(request.queryParam("asc").orElse("true"));

        return capacityServicePort.findPagedCapacities(page, size, sortBy, asc)
                .flatMap(pagedResponse ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(pagedResponse)
                )
                .contextWrite(ctx -> {
                    if (messageId != null) ctx = ctx.put(X_MESSAGE_ID, messageId);
                    if (token != null) ctx = ctx.put(AUTH_TOKEN, token);
                    return ctx;
                })
                .doOnError(ex -> log.error("Error fetching paged capacities. messageId={}", messageId, ex))
                .onErrorResume(ex ->
                        handlerUtils.buildErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                messageId,
                                TechnicalMessage.INTERNAL_ERROR,
                                List.of(ErrorDTO.builder()
                                        .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                        .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                        .build())));
    }
}
