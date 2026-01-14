package com.reactivo.capacidad.infrastructure.entrypoints.handler;

import com.reactivo.capacidad.domain.api.BootcampCapacityServicePort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.infrastructure.entrypoints.dto.request.BootcampCapacityDTO;
import com.reactivo.capacidad.infrastructure.entrypoints.dto.request.BootcampIdsRequest;
import com.reactivo.capacidad.infrastructure.entrypoints.mapper.BootcampCapacityMapper;
import com.reactivo.capacidad.infrastructure.entrypoints.util.Constants;
import com.reactivo.capacidad.infrastructure.entrypoints.util.ContextKeys;
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

import static com.reactivo.capacidad.infrastructure.entrypoints.util.Constants.HEADER_REQUIRED;
import static com.reactivo.capacidad.infrastructure.entrypoints.util.ContextKeys.AUTH_TOKEN;
import static com.reactivo.capacidad.infrastructure.entrypoints.util.ContextKeys.X_MESSAGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootcampCapacityHandler {

    private final BootcampCapacityServicePort bootcampCapacityServicePort;
    private final BootcampCapacityMapper mapper;
    private final HandlerUtils handlerUtils;

    public Mono<ServerResponse> saveAllBoorcampCapacity(ServerRequest request) {

        String messageId = handlerUtils.getMessageId(request);
        if (messageId == null) {
            return handlerUtils.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    null,
                    TechnicalMessage.INVALID_PARAMETERS,
                    List.of(ErrorDTO.builder()
                            .code(TechnicalMessage.INVALID_PARAMETERS.getCode())
                            .message(HEADER_REQUIRED)
                            .build()));
        }

        Flux<BootcampCapacity> bootcampCapacityFlux =
                request.bodyToFlux(BootcampCapacityDTO.class)
                        .map(mapper::toModel)
                        .filter(Objects::nonNull);

        return bootcampCapacityServicePort.saveAllBootcampCapacity(bootcampCapacityFlux)
                .collectList()
                .flatMap(list ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(list))
                .contextWrite(ctx -> ctx.put(ContextKeys.X_MESSAGE_ID, messageId))
                .doOnSuccess(v ->
                        log.info("CapacityTechnology saved successfully. messageId={}", messageId))
                .doOnError(ex ->
                        log.error("Error saving CapacityTechnology. messageId={}", messageId, ex))
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

    public Mono<ServerResponse> getCapacitiesByBootcampIds(ServerRequest request) {
        String messageId = handlerUtils.getMessageId(request);
        String token = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        if (messageId == null) {
            return handlerUtils.buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    null,
                    TechnicalMessage.INVALID_PARAMETERS,
                    List.of(ErrorDTO.builder()
                            .code(TechnicalMessage.INVALID_PARAMETERS.getCode())
                            .message("Header x-message-id is required")
                            .build()));
        }

        return request.bodyToMono(BootcampIdsRequest.class)
                .flatMap(req -> {
                    List<Long> bootcampIds = req.getBootcampIds();
                    if (bootcampIds == null || bootcampIds.isEmpty()) {
                        return handlerUtils.buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                messageId,
                                TechnicalMessage.INVALID_PARAMETERS,
                                List.of(ErrorDTO.builder()
                                        .code(TechnicalMessage.INVALID_PARAMETERS.getCode())
                                        .message("bootcampIds cannot be empty")
                                        .build()));
                    }

                    return bootcampCapacityServicePort.findCapacitiesByBootcampIds(bootcampIds)
                            .flatMap(result -> {
                                ServerResponse.BodyBuilder responseBuilder = ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .header("x-message-id", messageId);

                                if (token != null) {
                                    responseBuilder.header(HttpHeaders.AUTHORIZATION, token);
                                }

                                return responseBuilder.bodyValue(result);
                            })
                            .contextWrite(ctx -> {
                                ctx = ctx.put(Constants.X_MESSAGE_ID, messageId);
                                if (token != null) ctx = ctx.put(AUTH_TOKEN, token);
                                return ctx;
                            })
                            .doOnSuccess(v -> log.info("Fetched capacities by bootcampIds. messageId={}", messageId))
                            .doOnError(ex -> log.error("Error fetching capacities by bootcampIds. messageId={}", messageId, ex))
                            .onErrorResume(ex -> handlerUtils.buildErrorResponse(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    messageId,
                                    TechnicalMessage.INTERNAL_ERROR,
                                    List.of(ErrorDTO.builder()
                                            .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                            .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                            .build())));
                });
    }


    public Mono<ServerResponse> getBootcampIdGroupedCapacities(ServerRequest request) {
        String messageId = handlerUtils.getMessageId(request);
        String token = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        boolean asc = Boolean.parseBoolean(request.queryParam("asc").orElse("true"));

        return bootcampCapacityServicePort.getBootcampIdGroupedCapacities(page, size, asc)
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result))
                .contextWrite(ctx -> {
                    if (messageId != null) ctx = ctx.put(Constants.X_MESSAGE_ID, messageId);
                    if (token != null) ctx = ctx.put(AUTH_TOKEN, token);
                    return ctx;
                })
                .doOnSuccess(v -> log.info("Fetched paged capacities. page={}, size={}, asc={}, messageId={}", page, size, asc, messageId))
                .doOnError(ex -> log.error("Error fetching paged capacities. page={}, size={}, asc={}, messageId={}", page, size, asc, messageId, ex))
                .onErrorResume(ex -> handlerUtils.buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        messageId,
                        TechnicalMessage.INTERNAL_ERROR,
                        List.of(ErrorDTO.builder()
                                .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                .build())));
    }


}
