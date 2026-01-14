package com.reactivo.capacidad.infrastructure.entrypoints.util;

import com.reactivo.capacidad.domain.enums.TechnicalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.reactivo.capacidad.infrastructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandlerUtils {

    public Mono<ServerResponse> buildErrorResponse(HttpStatus httpStatus,
                                                   String identifier,
                                                   TechnicalMessage error,
                                                   List<ErrorDTO> errors) {
        return Mono.defer(() -> {
            APIResponse apiErrorResponse = APIResponse.builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .identifier(identifier)
                    .date(Instant.now().toString())
                    .errors(errors)
                    .build();
            return ServerResponse.status(httpStatus)
                    .bodyValue(apiErrorResponse);
        });
    }

    public String getMessageId(ServerRequest serverRequest) {
        return serverRequest.headers().firstHeader(X_MESSAGE_ID);
    }

    public List<Long> getQueryParamAsList(ServerRequest request, String paramName, Function<String, Long> mapper) {
        return request.queryParam(paramName)
                .map(s -> Arrays.stream(s.split(","))
                        .map(mapper)
                        .filter(Objects::nonNull)
                        .toList()
                )
                .orElse(Collections.emptyList());
    }

    public int getQueryParamAsInt(ServerRequest request, String paramName, int defaultValue) {
        return request.queryParam(paramName)
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }

    public boolean getQueryParamAsBoolean(ServerRequest request, String paramName, boolean defaultValue) {
        return request.queryParam(paramName)
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }
}