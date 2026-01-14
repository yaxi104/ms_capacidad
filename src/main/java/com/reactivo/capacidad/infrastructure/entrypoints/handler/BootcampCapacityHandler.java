package com.reactivo.capacidad.infrastructure.entrypoints.handler;

import com.reactivo.capacidad.domain.api.BootcampCapacityServicePort;
import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import com.reactivo.capacidad.domain.model.BootcampCapacity;
import com.reactivo.capacidad.infrastructure.entrypoints.dto.request.BootcampCapacityDTO;
import com.reactivo.capacidad.infrastructure.entrypoints.mapper.BootcampCapacityMapper;
import com.reactivo.capacidad.infrastructure.entrypoints.util.ContextKeys;
import com.reactivo.capacidad.infrastructure.entrypoints.util.ErrorDTO;
import com.reactivo.capacidad.infrastructure.entrypoints.util.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

//    public Mono<ServerResponse> findAllIdTechnologyByIdCapacity(ServerRequest request) {
//
//        String messageId = handlerUtils.getMessageId(request);
//        if (messageId == null) {
//            return handlerUtils.buildErrorResponse(
//                    HttpStatus.BAD_REQUEST,
//                    null,
//                    TechnicalMessage.INVALID_PARAMETERS,
//                    List.of(ErrorDTO.builder()
//                            .code(TechnicalMessage.INVALID_PARAMETERS.getCode())
//                            .message(HEADER_REQUIRED)
//                            .build()));
//        }
//
//        Long idCapacity = Long.valueOf(request.pathVariable("idCapacity"));
//
//        return capacityTechnologyUseCase.findAllIdTechnologyByIdCapacity(idCapacity)
//                .collectList()
//                .flatMap(list ->
//                        ServerResponse.ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .bodyValue(list))
//                .contextWrite(ctx -> ctx.put(ContextKeys.X_MESSAGE_ID, messageId))
//                .doOnError(ex ->
//                        log.error("Error fetching technologies. capacityId={} messageId={}",
//                                idCapacity, messageId, ex))
//                .onErrorResume(ex ->
//                        handlerUtils.buildErrorResponse(
//                                HttpStatus.INTERNAL_SERVER_ERROR,
//                                messageId,
//                                TechnicalMessage.INTERNAL_ERROR,
//                                List.of(ErrorDTO.builder()
//                                        .code(TechnicalMessage.INTERNAL_ERROR.getCode())
//                                        .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
//                                        .build())));
//    }
//
//    public Mono<ServerResponse> getTechnologiesByCapacityIds(ServerRequest request) {
//        String messageId = handlerUtils.getMessageId(request);
//
//        return request.bodyToMono(CapacityIdsRequest.class)
//                .flatMap(req ->
//                        capacityTechnologyUseCase.findTechnologiesByCapacityIds(req.capacityIds())
//                )
//                .flatMap(resultMap ->
//                        ServerResponse.ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .bodyValue(resultMap)
//                )
//                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
//                .doOnError(ex ->
//                        log.error("Error fetching technologies by capacity ids, messageId: {}", messageId, ex))
//                .onErrorResume(ex ->
//                        handlerUtils.buildErrorResponse(
//                                HttpStatus.INTERNAL_SERVER_ERROR,
//                                messageId,
//                                TechnicalMessage.INTERNAL_ERROR,
//                                List.of(ErrorDTO.builder()
//                                        .code(TechnicalMessage.INTERNAL_ERROR.getCode())
//                                        .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
//                                        .build())));
//    }
//
//    public Mono<ServerResponse> getCapacityIdGroupedTechnologies(ServerRequest request) {
//        String messageId = handlerUtils.getMessageId(request);
//        if (messageId == null) {
//            return handlerUtils.buildErrorResponse(
//                    HttpStatus.BAD_REQUEST,
//                    null,
//                    TechnicalMessage.INVALID_PARAMETERS,
//                    List.of(ErrorDTO.builder()
//                            .code(TechnicalMessage.INVALID_PARAMETERS.getCode())
//                            .message(HEADER_REQUIRED)
//                            .build()));
//        }
//
//        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
//        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
//        boolean asc = request.queryParam("asc").map(Boolean::parseBoolean).orElse(true);
//
//        return capacityTechnologyUseCase
//                .getCapacidtyIdGroupedTechnologies(page, size, asc)
//                .flatMap(resultMap ->
//                        ServerResponse.ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .bodyValue(resultMap)
//                )
//                .contextWrite(ctx -> ctx.put(ContextKeys.X_MESSAGE_ID, messageId))
//                .doOnError(ex ->
//                        log.error("Error fetching grouped technologies, messageId={}", messageId, ex))
//                .onErrorResume(ex ->
//                        handlerUtils.buildErrorResponse(
//                                HttpStatus.INTERNAL_SERVER_ERROR,
//                                messageId,
//                                TechnicalMessage.INTERNAL_ERROR,
//                                List.of(ErrorDTO.builder()
//                                        .code(TechnicalMessage.INTERNAL_ERROR.getCode())
//                                        .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
//                                        .build())));
//    }

}
